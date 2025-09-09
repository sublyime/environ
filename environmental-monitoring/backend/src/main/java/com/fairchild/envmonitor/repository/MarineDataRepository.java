package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.MarineData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MarineDataRepository extends JpaRepository<MarineData, Long> {

    List<MarineData> findByStationIdOrderByTimestampDesc(String stationId);

    List<MarineData> findByStationIdAndTimestampBetweenOrderByTimestampDesc(
            String stationId, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT m FROM MarineData m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<MarineData> findRecentMarineData(@Param("since") OffsetDateTime since);

    @Query("SELECT DISTINCT m.stationId FROM MarineData m ORDER BY m.stationId")
    List<String> findDistinctStationIds();

    @Query("SELECT m FROM MarineData m WHERE m.stationId = :stationId ORDER BY m.timestamp DESC LIMIT 1")
    MarineData findLatestByStationId(@Param("stationId") String stationId);
}
