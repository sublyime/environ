package com.fairchild.envmonitor.repository;

import com.fairchild.envmonitor.entity.WebcamData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebcamDataRepository extends JpaRepository<WebcamData, Long> {

    Optional<WebcamData> findByWebcamId(String webcamId);

    List<WebcamData> findByIsActiveTrue();

    List<WebcamData> findByCategory(String category);

    List<WebcamData> findByCategoryAndIsActiveTrue(String category);

    @Query("SELECT w FROM WebcamData w WHERE w.location ILIKE %:location% AND w.isActive = true")
    List<WebcamData> findByLocationContainingIgnoreCase(@Param("location") String location);

    @Query("SELECT DISTINCT w.category FROM WebcamData w WHERE w.category IS NOT NULL ORDER BY w.category")
    List<String> findDistinctCategories();
}
