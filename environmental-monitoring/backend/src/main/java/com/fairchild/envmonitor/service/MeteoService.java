package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.MeteoData;
import com.fairchild.envmonitor.repository.MeteoDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MeteoService {

    private static final Logger logger = LoggerFactory.getLogger(MeteoService.class);
    private final WebClient openMeteoWebClient;
    private final MeteoDataRepository meteoDataRepository;
    private final DataSourceStatusService dataSourceStatusService;
    private final ObjectMapper objectMapper;

    // Default locations for weather monitoring
    private static final Location[] DEFAULT_LOCATIONS = {
            new Location(new BigDecimal("41.8781"), new BigDecimal("-87.6298")), // Chicago
            new Location(new BigDecimal("34.0522"), new BigDecimal("-118.2437")), // Los Angeles
            new Location(new BigDecimal("40.7128"), new BigDecimal("-74.0060")), // New York
            new Location(new BigDecimal("39.7392"), new BigDecimal("-104.9903")), // Denver
            new Location(new BigDecimal("29.7604"), new BigDecimal("-95.3698")), // Houston
            new Location(new BigDecimal("47.6062"), new BigDecimal("-122.3321")), // Seattle
            new Location(new BigDecimal("25.7617"), new BigDecimal("-80.1918")), // Miami
            new Location(new BigDecimal("33.7490"), new BigDecimal("-84.3880")) // Atlanta
    };

    private static class Location {
        final BigDecimal latitude;
        final BigDecimal longitude;

        Location(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public MeteoService(@Qualifier("openMeteoWebClient") WebClient openMeteoWebClient,
            MeteoDataRepository meteoDataRepository,
            DataSourceStatusService dataSourceStatusService,
            ObjectMapper objectMapper) {
        this.openMeteoWebClient = openMeteoWebClient;
        this.meteoDataRepository = meteoDataRepository;
        this.dataSourceStatusService = dataSourceStatusService;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> fetchAndStoreMeteoData() {
        logger.info("Starting Open-Meteo weather data fetch for all default locations");

        return Mono.fromRunnable(() -> {
            for (Location location : DEFAULT_LOCATIONS) {
                fetchLocationData(location.latitude, location.longitude)
                        .doOnSuccess(data -> logger.info("Successfully fetched meteo data for location: {}, {}",
                                location.latitude, location.longitude))
                        .doOnError(error -> logger.error("Failed to fetch meteo data for location: {}, {}",
                                location.latitude, location.longitude, error))
                        .subscribe();
            }
        });
    }

    public Mono<MeteoData> fetchLocationData(BigDecimal latitude, BigDecimal longitude) {
        return openMeteoWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current",
                                "temperature_2m,relative_humidity_2m,precipitation,wind_speed_10m,wind_direction_10m,uv_index")
                        .queryParam("timezone", "auto")
                        .queryParam("forecast_days", "1")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> parseMeteoResponse(response, latitude, longitude))
                .doOnSuccess(meteoData -> {
                    if (meteoData != null) {
                        meteoDataRepository.save(meteoData);
                        dataSourceStatusService.recordSuccessfulFetch("open-meteo");
                    }
                })
                .doOnError(error -> {
                    logger.error("Error fetching meteo data for location {}, {}: {}",
                            latitude, longitude, error.getMessage());
                    dataSourceStatusService.recordError("open-meteo", error.getMessage());
                });
    }

    private MeteoData parseMeteoResponse(JsonNode response, BigDecimal latitude, BigDecimal longitude) {
        try {
            JsonNode current = response.get("current");
            if (current == null) {
                logger.warn("No current data found in meteo response for location: {}, {}", latitude, longitude);
                return null;
            }

            MeteoData meteoData = new MeteoData();
            meteoData.setLatitude(latitude);
            meteoData.setLongitude(longitude);

            // Parse timestamp
            String timestampStr = current.get("time").asText();
            meteoData.setTimestamp(OffsetDateTime.parse(timestampStr + ":00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atOffset(OffsetDateTime.now().getOffset()));

            // Parse temperature (already in Celsius, convert to Fahrenheit)
            JsonNode tempNode = current.get("temperature_2m");
            if (tempNode != null && !tempNode.isNull()) {
                BigDecimal tempCelsius = new BigDecimal(tempNode.asDouble());
                BigDecimal tempFahrenheit = tempCelsius.multiply(new BigDecimal("1.8")).add(new BigDecimal("32"));
                meteoData.setTemperature2m(tempFahrenheit);
            }

            // Parse humidity
            JsonNode humidityNode = current.get("relative_humidity_2m");
            if (humidityNode != null && !humidityNode.isNull()) {
                meteoData.setRelativeHumidity2m(new BigDecimal(humidityNode.asDouble()));
            }

            // Parse precipitation
            JsonNode precipNode = current.get("precipitation");
            if (precipNode != null && !precipNode.isNull()) {
                meteoData.setPrecipitation(new BigDecimal(precipNode.asDouble()));
            }

            // Parse wind speed (convert from m/s to mph)
            JsonNode windSpeedNode = current.get("wind_speed_10m");
            if (windSpeedNode != null && !windSpeedNode.isNull()) {
                BigDecimal windSpeedMs = new BigDecimal(windSpeedNode.asDouble());
                BigDecimal windSpeedMph = windSpeedMs.multiply(new BigDecimal("2.237"));
                meteoData.setWindSpeed10m(windSpeedMph);
            }

            // Parse wind direction
            JsonNode windDirNode = current.get("wind_direction_10m");
            if (windDirNode != null && !windDirNode.isNull()) {
                meteoData.setWindDirection10m(windDirNode.asInt());
            }

            // Parse UV index
            JsonNode uvNode = current.get("uv_index");
            if (uvNode != null && !uvNode.isNull()) {
                meteoData.setUvIndex(new BigDecimal(uvNode.asDouble()));
            }

            // Store raw data
            meteoData.setRawData(response);

            return meteoData;

        } catch (Exception e) {
            logger.error("Error parsing meteo response for location {}, {}: {}", latitude, longitude, e.getMessage());
            return null;
        }
    }

    public List<MeteoData> getRecentMeteoData(int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return meteoDataRepository.findRecentMeteoData(since);
    }

    public List<MeteoData> getLocationData(BigDecimal latitude, BigDecimal longitude) {
        return meteoDataRepository.findByLocationOrderByTimestampDesc(latitude, longitude);
    }

    public List<MeteoData> getDataByBoundingBox(BigDecimal latMin, BigDecimal latMax,
            BigDecimal lonMin, BigDecimal lonMax, int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return meteoDataRepository.findByBoundingBoxAndTimestamp(latMin, latMax, lonMin, lonMax, since);
    }
}
