package com.fairchild.envmonitor.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meteo_data")
public class MeteoData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "temperature_2m", precision = 5, scale = 2)
    private BigDecimal temperature2m;

    @Column(name = "relative_humidity_2m", precision = 5, scale = 2)
    private BigDecimal relativeHumidity2m;

    @Column(name = "precipitation", precision = 6, scale = 2)
    private BigDecimal precipitation;

    @Column(name = "wind_speed_10m", precision = 5, scale = 2)
    private BigDecimal windSpeed10m;

    @Column(name = "wind_direction_10m")
    private Integer windDirection10m;

    @Column(name = "uv_index", precision = 4, scale = 2)
    private BigDecimal uvIndex;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private JsonNode rawData;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    // Constructors
    public MeteoData() {
    }

    // Getters and Setters
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

    public JsonNode getRawData() {
        return rawData;
    }

    public void setRawData(JsonNode rawData) {
        this.rawData = rawData;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
