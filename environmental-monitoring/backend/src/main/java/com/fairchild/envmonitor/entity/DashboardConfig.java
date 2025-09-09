package com.fairchild.envmonitor.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dashboard_configs")
public class DashboardConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "config_name", nullable = false)
    private String configName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layout_config", nullable = false, columnDefinition = "jsonb")
    private JsonNode layoutConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_sources", nullable = false, columnDefinition = "jsonb")
    private JsonNode dataSources;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "refresh_intervals", columnDefinition = "jsonb")
    private JsonNode refreshIntervals;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Constructors
    public DashboardConfig() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public JsonNode getLayoutConfig() {
        return layoutConfig;
    }

    public void setLayoutConfig(JsonNode layoutConfig) {
        this.layoutConfig = layoutConfig;
    }

    public JsonNode getDataSources() {
        return dataSources;
    }

    public void setDataSources(JsonNode dataSources) {
        this.dataSources = dataSources;
    }

    public JsonNode getRefreshIntervals() {
        return refreshIntervals;
    }

    public void setRefreshIntervals(JsonNode refreshIntervals) {
        this.refreshIntervals = refreshIntervals;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
