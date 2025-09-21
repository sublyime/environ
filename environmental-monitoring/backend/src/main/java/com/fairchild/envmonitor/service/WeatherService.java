package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.WeatherData;
import com.fairchild.envmonitor.repository.WeatherDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private final WebClient weatherGovWebClient;
    private final WeatherDataRepository weatherDataRepository;
    private final DataSourceStatusService dataSourceStatusService;
    private final ObjectMapper objectMapper;

    // Default weather stations for different regions
    private static final String[] DEFAULT_STATIONS = {
            "KORD", // Chicago O'Hare
            "KLAX", // Los Angeles
            "KJFK", // JFK New York
            "KDEN", // Denver
            "KIAH", // Houston
            "KSEA", // Seattle
            "KMIA", // Miami
            "KATL" // Atlanta
    };

    public WeatherService(@Qualifier("weatherGovWebClient") WebClient weatherGovWebClient,
            WeatherDataRepository weatherDataRepository,
            DataSourceStatusService dataSourceStatusService,
            ObjectMapper objectMapper) {
        this.weatherGovWebClient = weatherGovWebClient;
        this.weatherDataRepository = weatherDataRepository;
        this.dataSourceStatusService = dataSourceStatusService;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> fetchAndStoreWeatherDataAsync() {
        logger.info("Starting async weather data fetch for all default stations");

        return CompletableFuture.runAsync(() -> {
            for (String stationId : DEFAULT_STATIONS) {
                fetchStationData(stationId)
                        .doOnSuccess(
                                data -> logger.info("Successfully fetched weather data for station: {}", stationId))
                        .doOnError(
                                error -> logger.error("Failed to fetch weather data for station: {}", stationId, error))
                        .subscribe();
            }
        });
    }

    public Mono<Void> fetchAndStoreWeatherData() {
        logger.info("Starting weather data fetch for all default stations");

        return Mono.fromRunnable(() -> {
            for (String stationId : DEFAULT_STATIONS) {
                fetchStationData(stationId)
                        .doOnSuccess(
                                data -> logger.info("Successfully fetched weather data for station: {}", stationId))
                        .doOnError(
                                error -> logger.error("Failed to fetch weather data for station: {}", stationId, error))
                        .subscribe();
            }
        });
    }

    public Mono<WeatherData> fetchStationData(String stationId) {
        return weatherGovWebClient
                .get()
                .uri("/stations/{stationId}/observations/latest", stationId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> parseWeatherResponse(response, stationId))
                .doOnSuccess(weatherData -> {
                    if (weatherData != null) {
                        weatherDataRepository.save(weatherData);
                        dataSourceStatusService.recordSuccessfulFetch("weather.gov");
                    }
                })
                .doOnError(error -> {
                    logger.error("Error fetching weather data for station {}: {}", stationId, error.getMessage());
                    dataSourceStatusService.recordError("weather.gov", error.getMessage());
                });
    }

    private WeatherData parseWeatherResponse(JsonNode response, String stationId) {
        try {
            JsonNode properties = response.get("properties");
            if (properties == null) {
                logger.warn("No properties found in weather response for station: {}", stationId);
                return null;
            }

            WeatherData weatherData = new WeatherData();
            weatherData.setStationId(stationId);

            // Parse timestamp
            String timestampStr = properties.get("timestamp").asText();
            weatherData.setTimestamp(OffsetDateTime.parse(timestampStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            // Parse temperature (convert from Celsius to Fahrenheit for US users)
            JsonNode tempNode = properties.get("temperature");
            if (tempNode != null && !tempNode.isNull()) {
                BigDecimal tempCelsius = new BigDecimal(tempNode.get("value").asDouble());
                BigDecimal tempFahrenheit = tempCelsius.multiply(new BigDecimal("1.8")).add(new BigDecimal("32"));
                weatherData.setTemperature(tempFahrenheit);
            }

            // Parse humidity
            JsonNode humidityNode = properties.get("relativeHumidity");
            if (humidityNode != null && !humidityNode.isNull()) {
                weatherData.setHumidity(new BigDecimal(humidityNode.get("value").asDouble()));
            }

            // Parse pressure (convert from Pa to inHg)
            JsonNode pressureNode = properties.get("barometricPressure");
            if (pressureNode != null && !pressureNode.isNull()) {
                BigDecimal pressurePa = new BigDecimal(pressureNode.get("value").asDouble());
                BigDecimal pressureInHg = pressurePa.multiply(new BigDecimal("0.0002953"));
                weatherData.setPressure(pressureInHg);
            }

            // Parse wind speed (convert from m/s to mph)
            JsonNode windSpeedNode = properties.get("windSpeed");
            if (windSpeedNode != null && !windSpeedNode.isNull()) {
                BigDecimal windSpeedMs = new BigDecimal(windSpeedNode.get("value").asDouble());
                BigDecimal windSpeedMph = windSpeedMs.multiply(new BigDecimal("2.237"));
                weatherData.setWindSpeed(windSpeedMph);
            }

            // Parse wind direction
            JsonNode windDirNode = properties.get("windDirection");
            if (windDirNode != null && !windDirNode.isNull()) {
                weatherData.setWindDirection(windDirNode.get("value").asInt());
            }

            // Parse visibility (convert from m to miles)
            JsonNode visibilityNode = properties.get("visibility");
            if (visibilityNode != null && !visibilityNode.isNull()) {
                BigDecimal visibilityM = new BigDecimal(visibilityNode.get("value").asDouble());
                BigDecimal visibilityMiles = visibilityM.multiply(new BigDecimal("0.000621371"));
                weatherData.setVisibility(visibilityMiles);
            }

            // Parse weather conditions
            JsonNode conditionsNode = properties.get("textDescription");
            if (conditionsNode != null && !conditionsNode.isNull()) {
                weatherData.setWeatherConditions(conditionsNode.asText());
            }

            // Store raw data
            weatherData.setRawData(response);

            return weatherData;

        } catch (Exception e) {
            logger.error("Error parsing weather response for station {}: {}", stationId, e.getMessage());
            return null;
        }
    }

    @Cacheable(value = "weatherData", key = "#hours")
    public List<WeatherData> getRecentWeatherData(int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return weatherDataRepository.findRecentWeatherData(since);
    }

    @Cacheable(value = "stationData", key = "#stationId")
    public List<WeatherData> getStationData(String stationId) {
        return weatherDataRepository.findByStationIdOrderByTimestampDesc(stationId);
    }

    @Cacheable(value = "availableStations")
    public List<String> getAvailableStations() {
        return weatherDataRepository.findDistinctStationIds();
    }

    @Cacheable(value = "latestStationData", key = "#stationId")
    public WeatherData getLatestStationData(String stationId) {
        return weatherDataRepository.findLatestByStationId(stationId);
    }
}
