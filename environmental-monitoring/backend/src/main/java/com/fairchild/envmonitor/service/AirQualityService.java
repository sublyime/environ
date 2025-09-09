package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.AirQualityData;
import com.fairchild.envmonitor.repository.AirQualityDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AirQualityService {

    private static final Logger logger = LoggerFactory.getLogger(AirQualityService.class);
    private final WebClient airQualityWebClient;
    private final AirQualityDataRepository airQualityDataRepository;
    private final DataSourceStatusService dataSourceStatusService;
    private final ObjectMapper objectMapper;

    @Value("${external-apis.air-quality.api-key}")
    private String apiKey;

    // Default locations for air quality monitoring
    private static final AQLocation[] DEFAULT_AQ_LOCATIONS = {
            new AQLocation("Chicago", new BigDecimal("41.8781"), new BigDecimal("-87.6298")),
            new AQLocation("Los Angeles", new BigDecimal("34.0522"), new BigDecimal("-118.2437")),
            new AQLocation("New York", new BigDecimal("40.7128"), new BigDecimal("-74.0060")),
            new AQLocation("Denver", new BigDecimal("39.7392"), new BigDecimal("-104.9903")),
            new AQLocation("Houston", new BigDecimal("29.7604"), new BigDecimal("-95.3698")),
            new AQLocation("Seattle", new BigDecimal("47.6062"), new BigDecimal("-122.3321")),
            new AQLocation("Miami", new BigDecimal("25.7617"), new BigDecimal("-80.1918")),
            new AQLocation("Atlanta", new BigDecimal("33.7490"), new BigDecimal("-84.3880"))
    };

    private static class AQLocation {
        final String name;
        final BigDecimal latitude;
        final BigDecimal longitude;

        AQLocation(String name, BigDecimal latitude, BigDecimal longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public AirQualityService(@Qualifier("airQualityWebClient") WebClient airQualityWebClient,
            AirQualityDataRepository airQualityDataRepository,
            DataSourceStatusService dataSourceStatusService,
            ObjectMapper objectMapper) {
        this.airQualityWebClient = airQualityWebClient;
        this.airQualityDataRepository = airQualityDataRepository;
        this.dataSourceStatusService = dataSourceStatusService;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> fetchAndStoreAirQualityData() {
        logger.info("Starting air quality data fetch for all default locations");

        return Mono.fromRunnable(() -> {
            for (AQLocation location : DEFAULT_AQ_LOCATIONS) {
                fetchLocationData(location.name, location.latitude, location.longitude)
                        .doOnSuccess(data -> logger.info("Successfully fetched air quality data for location: {}",
                                location.name))
                        .doOnError(error -> logger.error("Failed to fetch air quality data for location: {}",
                                location.name, error))
                        .subscribe();
            }
        });
    }

    public Mono<AirQualityData> fetchLocationData(String stationId, BigDecimal latitude, BigDecimal longitude) {
        return airQualityWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/observation/latLong/current/")
                        .queryParam("format", "application/json")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("distance", "25")
                        .queryParam("API_KEY", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> parseAirQualityResponse(response, stationId, latitude, longitude))
                .doOnSuccess(airQualityData -> {
                    if (airQualityData != null) {
                        airQualityDataRepository.save(airQualityData);
                        dataSourceStatusService.recordSuccessfulFetch("air-quality");
                    }
                })
                .doOnError(error -> {
                    logger.error("Error fetching air quality data for location {}: {}", stationId, error.getMessage());
                    dataSourceStatusService.recordError("air-quality", error.getMessage());
                });
    }

    private AirQualityData parseAirQualityResponse(JsonNode response, String stationId,
            BigDecimal latitude, BigDecimal longitude) {
        try {
            if (response == null || !response.isArray() || response.size() == 0) {
                logger.warn("No air quality data found for location: {}", stationId);
                return null;
            }

            // Get the first (closest) monitoring station data
            JsonNode stationData = response.get(0);

            AirQualityData airQualityData = new AirQualityData();
            airQualityData.setStationId(stationId);
            airQualityData.setLatitude(latitude);
            airQualityData.setLongitude(longitude);
            airQualityData.setTimestamp(OffsetDateTime.now());

            // Parse AQI
            JsonNode aqiNode = stationData.get("AQI");
            if (aqiNode != null && !aqiNode.isNull()) {
                airQualityData.setAqi(aqiNode.asInt());
            }

            // Parse parameter-specific data
            String parameterName = stationData.get("ParameterName").asText();
            BigDecimal value = new BigDecimal(stationData.get("Value").asDouble());

            switch (parameterName.toUpperCase()) {
                case "PM2.5":
                    airQualityData.setPm25(value);
                    break;
                case "PM10":
                    airQualityData.setPm10(value);
                    break;
                case "NO2":
                    airQualityData.setNo2(value);
                    break;
                case "O3":
                    airQualityData.setO3(value);
                    break;
                case "SO2":
                    airQualityData.setSo2(value);
                    break;
                case "CO":
                    airQualityData.setCo(value);
                    break;
            }

            // Store raw data
            airQualityData.setRawData(response);

            return airQualityData;

        } catch (Exception e) {
            logger.error("Error parsing air quality response for location {}: {}", stationId, e.getMessage());
            return null;
        }
    }

    public List<AirQualityData> getRecentAirQualityData(int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return airQualityDataRepository.findRecentAirQualityData(since);
    }

    public List<AirQualityData> getStationData(String stationId) {
        return airQualityDataRepository.findByStationIdOrderByTimestampDesc(stationId);
    }

    public List<String> getAvailableStations() {
        return airQualityDataRepository.findDistinctStationIds();
    }

    public AirQualityData getLatestStationData(String stationId) {
        return airQualityDataRepository.findLatestByStationId(stationId);
    }

    public List<AirQualityData> getHighAqiReadings(Integer threshold, int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return airQualityDataRepository.findHighAqiReadings(threshold, since);
    }
}
