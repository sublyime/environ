package com.fairchild.envmonitor.scheduler;

import com.fairchild.envmonitor.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataFetchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataFetchScheduler.class);

    private final WeatherService weatherService;
    private final MeteoService meteoService;
    private final MarineDataService marineDataService;
    private final AirQualityService airQualityService;
    private final FireDataService fireDataService;

    public DataFetchScheduler(WeatherService weatherService,
            MeteoService meteoService,
            MarineDataService marineDataService,
            AirQualityService airQualityService,
            FireDataService fireDataService) {
        this.weatherService = weatherService;
        this.meteoService = meteoService;
        this.marineDataService = marineDataService;
        this.airQualityService = airQualityService;
        this.fireDataService = fireDataService;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void fetchWeatherData() {
        logger.info("Scheduled weather data fetch starting");
        weatherService.fetchAndStoreWeatherData().subscribe();
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void fetchMeteoData() {
        logger.info("Scheduled meteo data fetch starting");
        meteoService.fetchAndStoreMeteoData().subscribe();
    }

    @Scheduled(fixedRate = 600000) // 10 minutes
    public void fetchMarineData() {
        logger.info("Scheduled marine data fetch starting");
        marineDataService.fetchAndStoreMarineData().subscribe();
    }

    @Scheduled(fixedRate = 900000) // 15 minutes
    public void fetchAirQualityData() {
        logger.info("Scheduled air quality data fetch starting");
        airQualityService.fetchAndStoreAirQualityData().subscribe();
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void fetchFireData() {
        logger.info("Scheduled fire data fetch starting");
        fireDataService.fetchAndStoreFireData().subscribe();
    }
}
