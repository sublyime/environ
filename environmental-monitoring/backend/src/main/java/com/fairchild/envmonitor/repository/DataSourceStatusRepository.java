package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.DataSourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DataSourceStatusRepository extends JpaRepository<DataSourceStatus, Long> {

    Optional<DataSourceStatus> findBySourceName(String sourceName);
}
