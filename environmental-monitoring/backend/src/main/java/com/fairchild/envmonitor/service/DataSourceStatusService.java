package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.DataSourceStatus;
import com.fairchild.envmonitor.repository.DataSourceStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DataSourceStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceStatusService.class);
    private final DataSourceStatusRepository dataSourceStatusRepository;

    public DataSourceStatusService(DataSourceStatusRepository dataSourceStatusRepository) {
        this.dataSourceStatusRepository = dataSourceStatusRepository;
    }

    public void recordSuccessfulFetch(String sourceName) {
        Optional<DataSourceStatus> statusOpt = dataSourceStatusRepository.findBySourceName(sourceName);
        DataSourceStatus status = statusOpt.orElse(new DataSourceStatus());

        if (statusOpt.isEmpty()) {
            status.setSourceName(sourceName);
            status.setFetchCount(0);
            status.setErrorCount(0);
            status.setIsActive(true);
        }

        status.setLastSuccessfulFetch(OffsetDateTime.now());
        status.setFetchCount(status.getFetchCount() + 1);

        dataSourceStatusRepository.save(status);
        logger.debug("Recorded successful fetch for source: {}", sourceName);
    }

    public void recordError(String sourceName, String errorMessage) {
        Optional<DataSourceStatus> statusOpt = dataSourceStatusRepository.findBySourceName(sourceName);
        DataSourceStatus status = statusOpt.orElse(new DataSourceStatus());

        if (statusOpt.isEmpty()) {
            status.setSourceName(sourceName);
            status.setFetchCount(0);
            status.setErrorCount(0);
            status.setIsActive(true);
        }

        status.setLastError(OffsetDateTime.now());
        status.setErrorMessage(errorMessage);
        status.setErrorCount(status.getErrorCount() + 1);

        dataSourceStatusRepository.save(status);
        logger.warn("Recorded error for source {}: {}", sourceName, errorMessage);
    }

    public List<DataSourceStatus> getAllDataSourceStatuses() {
        return dataSourceStatusRepository.findAll();
    }

    public Optional<DataSourceStatus> getDataSourceStatus(String sourceName) {
        return dataSourceStatusRepository.findBySourceName(sourceName);
    }

    public void toggleDataSourceActive(String sourceName, boolean isActive) {
        Optional<DataSourceStatus> statusOpt = dataSourceStatusRepository.findBySourceName(sourceName);
        if (statusOpt.isPresent()) {
            DataSourceStatus status = statusOpt.get();
            status.setIsActive(isActive);
            dataSourceStatusRepository.save(status);
            logger.info("Set data source {} active status to: {}", sourceName, isActive);
        }
    }
}
