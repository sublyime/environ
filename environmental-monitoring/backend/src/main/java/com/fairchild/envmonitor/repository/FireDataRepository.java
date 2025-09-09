package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.FireData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FireDataRepository extends JpaRepository<FireData, Long> {

    Optional<FireData> findByFireId(String fireId);

    List<FireData> findByFireStatus(String fireStatus);

    @Query("SELECT f FROM FireData f WHERE f.updatedAt >= :since ORDER BY f.updatedAt DESC")
    List<FireData> findRecentlyUpdated(@Param("since") OffsetDateTime since);

    @Query("SELECT f FROM FireData f WHERE f.latitude BETWEEN :latMin AND :latMax " +
            "AND f.longitude BETWEEN :lonMin AND :lonMax ORDER BY f.updatedAt DESC")
    List<FireData> findByBoundingBox(
            @Param("latMin") BigDecimal latMin, @Param("latMax") BigDecimal latMax,
            @Param("lonMin") BigDecimal lonMin, @Param("lonMax") BigDecimal lonMax);

    @Query("SELECT f FROM FireData f WHERE f.fireSizeAcres > :minSize ORDER BY f.fireSizeAcres DESC")
    List<FireData> findLargeFires(@Param("minSize") BigDecimal minSize);

    @Query("SELECT DISTINCT f.fireStatus FROM FireData f WHERE f.fireStatus IS NOT NULL ORDER BY f.fireStatus")
    List<String> findDistinctFireStatuses();
}
