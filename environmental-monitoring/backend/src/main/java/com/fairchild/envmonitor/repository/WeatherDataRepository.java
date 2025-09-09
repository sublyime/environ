package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    List<WeatherData> findByStationIdOrderByTimestampDesc(String stationId);

    List<WeatherData> findByStationIdAndTimestampBetweenOrderByTimestampDesc(
            String stationId, OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT w FROM WeatherData w WHERE w.timestamp >= :since ORDER BY w.timestamp DESC")
    List<WeatherData> findRecentWeatherData(@Param("since") OffsetDateTime since);

    @Query("SELECT DISTINCT w.stationId FROM WeatherData w ORDER BY w.stationId")
    List<String> findDistinctStationIds();

    @Query("SELECT w FROM WeatherData w WHERE w.stationId = :stationId ORDER BY w.timestamp DESC LIMIT 1")
    WeatherData findLatestByStationId(@Param("stationId") String stationId);
}
