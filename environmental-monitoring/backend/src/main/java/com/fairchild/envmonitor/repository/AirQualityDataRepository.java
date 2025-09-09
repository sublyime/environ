package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.AirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AirQualityDataRepository extends JpaRepository<AirQualityData, Long> {

    List<AirQualityData> findByStationIdOrderByTimestampDesc(String stationId);

    List<AirQualityData> findByStationIdAndTimestampBetweenOrderByTimestampDesc(
            String stationId, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT a FROM AirQualityData a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AirQualityData> findRecentAirQualityData(@Param("since") OffsetDateTime since);

    @Query("SELECT DISTINCT a.stationId FROM AirQualityData a ORDER BY a.stationId")
    List<String> findDistinctStationIds();

    @Query("SELECT a FROM AirQualityData a WHERE a.stationId = :stationId ORDER BY a.timestamp DESC LIMIT 1")
    AirQualityData findLatestByStationId(@Param("stationId") String stationId);

    @Query("SELECT a FROM AirQualityData a WHERE a.aqi > :threshold AND a.timestamp >= :since ORDER BY a.aqi DESC")
    List<AirQualityData> findHighAqiReadings(@Param("threshold") Integer threshold,
            @Param("since") OffsetDateTime since);
}
