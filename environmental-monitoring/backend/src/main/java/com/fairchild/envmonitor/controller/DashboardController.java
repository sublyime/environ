package com.fairchild.envmonitor.controller;

import com.fairchild.envmonitor.dto.*;
import com.fairchild.envmonitor.service.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final WeatherService weatherService;
    private final MeteoService meteoService;
    private final MarineDataService marineDataService;
    private final AirQualityService airQualityService;
    private final FireDataService fireDataService;
    private final WebcamDataService webcamDataService;
    private final DataSourceStatusService dataSourceStatusService;

    public DashboardController(WeatherService weatherService,
            MeteoService meteoService,
            MarineDataService marineDataService,
            AirQualityService airQualityService,
            FireDataService fireDataService,
            WebcamDataService webcamDataService,
            DataSourceStatusService dataSourceStatusService) {
        this.weatherService = weatherService;
        this.meteoService = meteoService;
        this.marineDataService = marineDataService;
        this.airQualityService = airQualityService;
        this.fireDataService = fireDataService;
        this.webcamDataService = webcamDataService;
        this.dataSourceStatusService = dataSourceStatusService;
    }

    @GetMapping("/data")
    @Cacheable(value = "dashboardData", key = "#hours")
    public ResponseEntity<DashboardDataDto> getDashboardData(@RequestParam(defaultValue = "24") int hours) {
        logger.info("Fetching dashboard data for the last {} hours", hours);

        try {
            // Execute all data fetching operations in parallel
            CompletableFuture<List<WeatherDataDto>> weatherFuture = CompletableFuture
                    .supplyAsync(() -> weatherService.getRecentWeatherData(hours).stream()
                            .map(this::convertToWeatherDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<MeteoDataDto>> meteoFuture = CompletableFuture
                    .supplyAsync(() -> meteoService.getRecentMeteoData(hours).stream()
                            .map(this::convertToMeteoDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<MarineDataDto>> marineFuture = CompletableFuture
                    .supplyAsync(() -> marineDataService.getRecentMarineData(hours).stream()
                            .map(this::convertToMarineDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<AirQualityDataDto>> airQualityFuture = CompletableFuture
                    .supplyAsync(() -> airQualityService.getRecentAirQualityData(hours).stream()
                            .map(this::convertToAirQualityDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<FireDataDto>> fireFuture = CompletableFuture
                    .supplyAsync(() -> fireDataService.getRecentlyUpdatedFires(hours).stream()
                            .map(this::convertToFireDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<WebcamDataDto>> webcamFuture = CompletableFuture
                    .supplyAsync(() -> webcamDataService.getActiveWebcams().stream()
                            .map(this::convertToWebcamDto)
                            .collect(Collectors.toList()));

            CompletableFuture<List<DataSourceStatusDto>> statusFuture = CompletableFuture
                    .supplyAsync(() -> dataSourceStatusService.getAllDataSourceStatuses().stream()
                            .map(this::convertToDataSourceStatusDto)
                            .collect(Collectors.toList()));

            // Wait for all futures to complete
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    weatherFuture, meteoFuture, marineFuture, airQualityFuture,
                    fireFuture, webcamFuture, statusFuture);

            allFutures.get(); // Wait for completion

            // Build response
            DashboardDataDto dashboardData = new DashboardDataDto();
            dashboardData.setRecentWeatherData(weatherFuture.get());
            dashboardData.setRecentMeteoData(meteoFuture.get());
            dashboardData.setRecentMarineData(marineFuture.get());
            dashboardData.setRecentAirQualityData(airQualityFuture.get());
            dashboardData.setRecentFireData(fireFuture.get());
            dashboardData.setActiveWebcams(webcamFuture.get());
            dashboardData.setDataSourceStatuses(statusFuture.get());

            return ResponseEntity.ok(dashboardData);

        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error fetching dashboard data", e);
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            logger.error("Error fetching dashboard data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/refresh/{dataSource}")
    public ResponseEntity<String> refreshDataSource(@PathVariable String dataSource) {
        logger.info("Manual refresh requested for data source: {}", dataSource);

        try {
            switch (dataSource.toLowerCase()) {
                case "weather":
                    weatherService.fetchAndStoreWeatherData().subscribe();
                    break;
                case "meteo":
                    meteoService.fetchAndStoreMeteoData().subscribe();
                    break;
                case "marine":
                    marineDataService.fetchAndStoreMarineData().subscribe();
                    break;
                case "airquality":
                    airQualityService.fetchAndStoreAirQualityData().subscribe();
                    break;
                case "fire":
                    fireDataService.fetchAndStoreFireData().subscribe();
                    break;
                default:
                    return ResponseEntity.badRequest().body("Unknown data source: " + dataSource);
            }

            return ResponseEntity.ok("Refresh initiated for " + dataSource);

        } catch (Exception e) {
            logger.error("Error refreshing data source: {}", dataSource, e);
            return ResponseEntity.internalServerError().body("Error refreshing " + dataSource);
        }
    }

    // Conversion methods
    private WeatherDataDto convertToWeatherDto(com.fairchild.envmonitor.entity.WeatherData entity) {
        WeatherDataDto dto = new WeatherDataDto();
        dto.setId(entity.getId());
        dto.setStationId(entity.getStationId());
        dto.setTimestamp(entity.getTimestamp());
        dto.setTemperature(entity.getTemperature());
        dto.setHumidity(entity.getHumidity());
        dto.setPressure(entity.getPressure());
        dto.setWindSpeed(entity.getWindSpeed());
        dto.setWindDirection(entity.getWindDirection());
        dto.setVisibility(entity.getVisibility());
        dto.setWeatherConditions(entity.getWeatherConditions());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private MeteoDataDto convertToMeteoDto(com.fairchild.envmonitor.entity.MeteoData entity) {
        MeteoDataDto dto = new MeteoDataDto();
        dto.setId(entity.getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setTimestamp(entity.getTimestamp());
        dto.setTemperature2m(entity.getTemperature2m());
        dto.setRelativeHumidity2m(entity.getRelativeHumidity2m());
        dto.setPrecipitation(entity.getPrecipitation());
        dto.setWindSpeed10m(entity.getWindSpeed10m());
        dto.setWindDirection10m(entity.getWindDirection10m());
        dto.setUvIndex(entity.getUvIndex());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    // Add similar conversion methods for other DTOs...
    private MarineDataDto convertToMarineDto(com.fairchild.envmonitor.entity.MarineData entity) {
        // Implementation similar to above
        return new MarineDataDto();
    }

    private AirQualityDataDto convertToAirQualityDto(com.fairchild.envmonitor.entity.AirQualityData entity) {
        // Implementation similar to above
        return new AirQualityDataDto();
    }

    private FireDataDto convertToFireDto(com.fairchild.envmonitor.entity.FireData entity) {
        // Implementation similar to above
        return new FireDataDto();
    }

    private WebcamDataDto convertToWebcamDto(com.fairchild.envmonitor.entity.WebcamData entity) {
        // Implementation similar to above
        return new WebcamDataDto();
    }

    private DataSourceStatusDto convertToDataSourceStatusDto(com.fairchild.envmonitor.entity.DataSourceStatus entity) {
        // Implementation similar to above
        return new DataSourceStatusDto();
    }
}
