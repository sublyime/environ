package com.fairchild.envmonitor.dto;

import java.util.List;

public class DashboardDataDto {
    private List<WeatherDataDto> recentWeatherData;
    private List<MeteoDataDto> recentMeteoData;
    private List<MarineDataDto> recentMarineData;
    private List<AirQualityDataDto> recentAirQualityData;
    private List<FireDataDto> recentFireData;
    private List<WebcamDataDto> activeWebcams;
    private List<DataSourceStatusDto> dataSourceStatuses;

    // Constructors
    public DashboardDataDto() {
    }

    // Getters and Setters
    public List<WeatherDataDto> getRecentWeatherData() {
        return recentWeatherData;
    }

    public void setRecentWeatherData(List<WeatherDataDto> recentWeatherData) {
        this.recentWeatherData = recentWeatherData;
    }

    public List<MeteoDataDto> getRecentMeteoData() {
        return recentMeteoData;
    }

    public void setRecentMeteoData(List<MeteoDataDto> recentMeteoData) {
        this.recentMeteoData = recentMeteoData;
    }

    public List<MarineDataDto> getRecentMarineData() {
        return recentMarineData;
    }

    public void setRecentMarineData(List<MarineDataDto> recentMarineData) {
        this.recentMarineData = recentMarineData;
    }

    public List<AirQualityDataDto> getRecentAirQualityData() {
        return recentAirQualityData;
    }

    public void setRecentAirQualityData(List<AirQualityDataDto> recentAirQualityData) {
        this.recentAirQualityData = recentAirQualityData;
    }

    public List<FireDataDto> getRecentFireData() {
        return recentFireData;
    }

    public void setRecentFireData(List<FireDataDto> recentFireData) {
        this.recentFireData = recentFireData;
    }

    public List<WebcamDataDto> getActiveWebcams() {
        return activeWebcams;
    }

    public void setActiveWebcams(List<WebcamDataDto> activeWebcams) {
        this.activeWebcams = activeWebcams;
    }

    public List<DataSourceStatusDto> getDataSourceStatuses() {
        return dataSourceStatuses;
    }

    public void setDataSourceStatuses(List<DataSourceStatusDto> dataSourceStatuses) {
        this.dataSourceStatuses = dataSourceStatuses;
    }
}
