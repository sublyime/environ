package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.DashboardConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DashboardConfigRepository extends JpaRepository<DashboardConfig, Long> {

    List<DashboardConfig> findByUserId(String userId);

    Optional<DashboardConfig> findByUserIdAndConfigName(String userId, String configName);

    void deleteByUserIdAndConfigName(String userId, String configName);
}
