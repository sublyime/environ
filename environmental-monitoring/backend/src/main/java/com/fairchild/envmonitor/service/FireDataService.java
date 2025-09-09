package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.FireData;
import com.fairchild.envmonitor.repository.FireDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class FireDataService {

    private static final Logger logger = LoggerFactory.getLogger(FireDataService.class);
    private final WebClient genericWebClient;
    private final FireDataRepository fireDataRepository;
    private final DataSourceStatusService dataSourceStatusService;
    private final ObjectMapper objectMapper;

    // NIFC (National Interagency Fire Center) active fire perimeters URL
    private static final String FIRE_DATA_URL = "https://services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/Current_WildlandFire_Perimeters/FeatureServer/0/query";

    public FireDataService(@Qualifier("genericWebClient") WebClient genericWebClient,
            FireDataRepository fireDataRepository,
            DataSourceStatusService dataSourceStatusService,
            ObjectMapper objectMapper) {
        this.genericWebClient = genericWebClient;
        this.fireDataRepository = fireDataRepository;
        this.dataSourceStatusService = dataSourceStatusService;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> fetchAndStoreFireData() {
        logger.info("Starting fire data fetch from NIFC");

        return genericWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("services3.arcgis.com")
                        .path("/T4QMspbfLg3qTGWY/arcgis/rest/services/Current_WildlandFire_Perimeters/FeatureServer/0/query")
                        .queryParam("where", "1=1")
                        .queryParam("outFields", "*")
                        .queryParam("f", "json")
                        .queryParam("returnGeometry", "true")
                        .queryParam("spatialRel", "esriSpatialRelIntersects")
                        .queryParam("outSR", "4326")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseFireDataResponse)
                .doOnSuccess(count -> {
                    logger.info("Successfully processed {} fire records", count);
                    dataSourceStatusService.recordSuccessfulFetch("fire-data");
                })
                .doOnError(error -> {
                    logger.error("Error fetching fire data: {}", error.getMessage());
                    dataSourceStatusService.recordError("fire-data", error.getMessage());
                })
                .then();
    }

    private Integer parseFireDataResponse(JsonNode response) {
        try {
            JsonNode features = response.get("features");
            if (features == null || !features.isArray()) {
                logger.warn("No features found in fire data response");
                return 0;
            }

            int processedCount = 0;
            for (JsonNode feature : features) {
                FireData fireData = parseFireFeature(feature);
                if (fireData != null) {
                    // Check if fire already exists, update if it does
                    Optional<FireData> existing = fireDataRepository.findByFireId(fireData.getFireId());
                    if (existing.isPresent()) {
                        FireData existingFire = existing.get();
                        updateFireData(existingFire, fireData);
                        fireDataRepository.save(existingFire);
                    } else {
                        fireDataRepository.save(fireData);
                    }
                    processedCount++;
                }
            }

            return processedCount;

        } catch (Exception e) {
            logger.error("Error parsing fire data response: {}", e.getMessage());
            return 0;
        }
    }

    private FireData parseFireFeature(JsonNode feature) {
        try {
            JsonNode attributes = feature.get("attributes");
            JsonNode geometry = feature.get("geometry");

            if (attributes == null) {
                return null;
            }

            FireData fireData = new FireData();

            // Parse fire ID
            JsonNode incidentIdNode = attributes.get("INCIDENT_ID");
            if (incidentIdNode != null && !incidentIdNode.isNull()) {
                fireData.setFireId(incidentIdNode.asText());
            }

            // Parse fire name
            JsonNode nameNode = attributes.get("INCIDENT_NAME");
            if (nameNode != null && !nameNode.isNull()) {
                fireData.setName(nameNode.asText());
            }

            // Parse location from geometry centroid
            if (geometry != null && geometry.get("rings") != null) {
                // Calculate centroid of the polygon
                JsonNode rings = geometry.get("rings");
                if (rings.isArray() && rings.size() > 0) {
                    JsonNode firstRing = rings.get(0);
                    if (firstRing.isArray() && firstRing.size() > 0) {
                        // Use first coordinate as approximate center
                        JsonNode firstCoord = firstRing.get(0);
                        if (firstCoord.isArray() && firstCoord.size() >= 2) {
                            fireData.setLongitude(new BigDecimal(firstCoord.get(0).asDouble()));
                            fireData.setLatitude(new BigDecimal(firstCoord.get(1).asDouble()));
                        }
                    }
                }
            }

            // Parse discovery date
            JsonNode discoveryDateNode = attributes.get("DISCOVERY_DATE");
            if (discoveryDateNode != null && !discoveryDateNode.isNull()) {
                try {
                    long timestamp = discoveryDateNode.asLong();
                    fireData.setDiscoveryDate(LocalDate.ofEpochDay(timestamp / (1000 * 60 * 60 * 24)));
                } catch (Exception e) {
                    logger.debug("Could not parse discovery date for fire: {}", fireData.getFireId());
                }
            }

            // Parse containment date
            JsonNode containmentDateNode = attributes.get("CONTAINMENT_DATE");
            if (containmentDateNode != null && !containmentDateNode.isNull()) {
                try {
                    long timestamp = containmentDateNode.asLong();
                    fireData.setContainmentDate(LocalDate.ofEpochDay(timestamp / (1000 * 60 * 60 * 24)));
                } catch (Exception e) {
                    logger.debug("Could not parse containment date for fire: {}", fireData.getFireId());
                }
            }

            // Parse fire size
            JsonNode sizeNode = attributes.get("FIRE_SIZE");
            if (sizeNode != null && !sizeNode.isNull()) {
                fireData.setFireSizeAcres(new BigDecimal(sizeNode.asDouble()));
            }

            // Parse fire cause
            JsonNode causeNode = attributes.get("FIRE_CAUSE");
            if (causeNode != null && !causeNode.isNull()) {
                fireData.setFireCause(causeNode.asText());
            }

            // Parse fire status
            JsonNode statusNode = attributes.get("FIRE_STATUS");
            if (statusNode != null && !statusNode.isNull()) {
                fireData.setFireStatus(statusNode.asText());
            }

            // Parse incident type
            JsonNode typeNode = attributes.get("INCIDENT_TYPE");
            if (typeNode != null && !typeNode.isNull()) {
                fireData.setIncidentType(typeNode.asText());
            }

            // Store raw data
            fireData.setRawData(feature);

            return fireData;

        } catch (Exception e) {
            logger.error("Error parsing fire feature: {}", e.getMessage());
            return null;
        }
    }

    private void updateFireData(FireData existing, FireData newData) {
        // Update fields that might change
        if (newData.getName() != null)
            existing.setName(newData.getName());
        if (newData.getContainmentDate() != null)
            existing.setContainmentDate(newData.getContainmentDate());
        if (newData.getFireSizeAcres() != null)
            existing.setFireSizeAcres(newData.getFireSizeAcres());
        if (newData.getFireStatus() != null)
            existing.setFireStatus(newData.getFireStatus());
        if (newData.getRawData() != null)
            existing.setRawData(newData.getRawData());
    }

    public List<FireData> getRecentlyUpdatedFires(int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return fireDataRepository.findRecentlyUpdated(since);
    }

    public List<FireData> getFiresByStatus(String status) {
        return fireDataRepository.findByFireStatus(status);
    }

    public List<FireData> getFiresByBoundingBox(BigDecimal latMin, BigDecimal latMax,
            BigDecimal lonMin, BigDecimal lonMax) {
        return fireDataRepository.findByBoundingBox(latMin, latMax, lonMin, lonMax);
    }

    public List<FireData> getLargeFires(BigDecimal minSizeAcres) {
        return fireDataRepository.findLargeFires(minSizeAcres);
    }

    public List<String> getFireStatuses() {
        return fireDataRepository.findDistinctFireStatuses();
    }
}
