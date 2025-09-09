package com.fairchild.envmonitor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class MeteoDataDto {
    private Long id;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private OffsetDateTime timestamp;

    private BigDecimal temperature2m;
    private BigDecimal relativeHumidity2m;
    private BigDecimal precipitation;
    private BigDecimal windSpeed10m;
    private Integer windDirection10m;
    private BigDecimal uvIndex;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private OffsetDateTime createdAt;

    // Constructors and getters/setters
    public MeteoDataDto() {
    }

    // All getters and setters (following same pattern as WeatherDataDto)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(BigDecimal temperature2m) {
        this.temperature2m = temperature2m;
    }

    public BigDecimal getRelativeHumidity2m() {
        return relativeHumidity2m;
    }

    public void setRelativeHumidity2m(BigDecimal relativeHumidity2m) {
        this.relativeHumidity2m = relativeHumidity2m;
    }

    public BigDecimal getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(BigDecimal precipitation) {
        this.precipitation = precipitation;
    }

    public BigDecimal getWindSpeed10m() {
        return windSpeed10m;
    }

    public void setWindSpeed10m(BigDecimal windSpeed10m) {
        this.windSpeed10m = windSpeed10m;
    }

    public Integer getWindDirection10m() {
        return windDirection10m;
    }

    public void setWindDirection10m(Integer windDirection10m) {
        this.windDirection10m = windDirection10m;
    }

    public BigDecimal getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(BigDecimal uvIndex) {
        this.uvIndex = uvIndex;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
