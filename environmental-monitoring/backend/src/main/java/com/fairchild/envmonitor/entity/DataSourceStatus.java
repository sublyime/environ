package com.fairchild.envmonitor.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "data_source_status")
public class DataSourceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false, unique = true, length = 100)
    private String sourceName;

    @Column(name = "last_successful_fetch")
    private OffsetDateTime lastSuccessfulFetch;

    @Column(name = "last_error")
    private OffsetDateTime lastError;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "fetch_count")
    private Integer fetchCount = 0;

    @Column(name = "error_count")
    private Integer errorCount = 0;

    // Constructors
    public DataSourceStatus() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public OffsetDateTime getLastSuccessfulFetch() {
        return lastSuccessfulFetch;
    }

    public void setLastSuccessfulFetch(OffsetDateTime lastSuccessfulFetch) {
        this.lastSuccessfulFetch = lastSuccessfulFetch;
    }

    public OffsetDateTime getLastError() {
        return lastError;
    }

    public void setLastError(OffsetDateTime lastError) {
        this.lastError = lastError;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getFetchCount() {
        return fetchCount;
    }

    public void setFetchCount(Integer fetchCount) {
        this.fetchCount = fetchCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }
}
