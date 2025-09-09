package com.fairchild.envmonitor.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "marine_data")
public class MarineData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false, length = 50)
    private String stationId;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    @Column(name = "water_level", precision = 6, scale = 2)
    private BigDecimal waterLevel;

    @Column(name = "wave_height", precision = 5, scale = 2)
    private BigDecimal waveHeight;

    @Column(name = "wave_period", precision = 5, scale = 2)
    private BigDecimal wavePeriod;

    @Column(name = "wave_direction")
    private Integer waveDirection;

    @Column(name = "water_temperature", precision = 5, scale = 2)
    private BigDecimal waterTemperature;

    @Column(name = "salinity", precision = 5, scale = 2)
    private BigDecimal salinity;

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
    public MarineData() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
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

    public BigDecimal getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(BigDecimal waterLevel) {
        this.waterLevel = waterLevel;
    }

    public BigDecimal getWaveHeight() {
        return waveHeight;
    }

    public void setWaveHeight(BigDecimal waveHeight) {
        this.waveHeight = waveHeight;
    }

    public BigDecimal getWavePeriod() {
        return wavePeriod;
    }

    public void setWavePeriod(BigDecimal wavePeriod) {
        this.wavePeriod = wavePeriod;
    }

    public Integer getWaveDirection() {
        return waveDirection;
    }

    public void setWaveDirection(Integer waveDirection) {
        this.waveDirection = waveDirection;
    }

    public BigDecimal getWaterTemperature() {
        return waterTemperature;
    }

    public void setWaterTemperature(BigDecimal waterTemperature) {
        this.waterTemperature = waterTemperature;
    }

    public BigDecimal getSalinity() {
        return salinity;
    }

    public void setSalinity(BigDecimal salinity) {
        this.salinity = salinity;
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
