package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.MeteoData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MeteoDataRepository extends JpaRepository<MeteoData, Long> {

    @Query("SELECT m FROM MeteoData m WHERE m.latitude = :lat AND m.longitude = :lon ORDER BY m.timestamp DESC")
    List<MeteoData> findByLocationOrderByTimestampDesc(
            @Param("lat") BigDecimal latitude, @Param("lon") BigDecimal longitude);

    @Query("SELECT m FROM MeteoData m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<MeteoData> findRecentMeteoData(@Param("since") OffsetDateTime since);

    @Query("SELECT m FROM MeteoData m WHERE m.latitude BETWEEN :latMin AND :latMax " +
            "AND m.longitude BETWEEN :lonMin AND :lonMax AND m.timestamp >= :since " +
            "ORDER BY m.timestamp DESC")
    List<MeteoData> findByBoundingBoxAndTimestamp(
            @Param("latMin") BigDecimal latMin, @Param("latMax") BigDecimal latMax,
            @Param("lonMin") BigDecimal lonMin, @Param("lonMax") BigDecimal lonMax,
            @Param("since") OffsetDateTime since);
}
