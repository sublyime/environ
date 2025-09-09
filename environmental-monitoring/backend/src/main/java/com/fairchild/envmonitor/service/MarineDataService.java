package com.fairchild.envmonitor.service;

import com.fairchild.envmonitor.entity.MarineData;
import com.fairchild.envmonitor.repository.MarineDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MarineDataService {

    private static final Logger logger = LoggerFactory.getLogger(MarineDataService.class);
    private final WebClient marineDataWebClient;
    private final MarineDataRepository marineDataRepository;
    private final DataSourceStatusService dataSourceStatusService;
    private final ObjectMapper objectMapper;

    // Default NOAA stations for marine data
    private static final String[] DEFAULT_MARINE_STATIONS = {
            "8518750", // The Battery, NY
            "8443970", // Boston, MA
            "8452660", // Newport, RI
            "8531680", // Sandy Hook, NJ
            "8534720", // Atlantic City, NJ
            "8551910", // Cape May, NJ
            "8570283", // Ocean City Inlet, MD
            "8574680", // Chesapeake Bay Bridge Tunnel, VA
            "8638610", // Sewells Point, VA
            "8651370" // Duck, NC
    };

    public MarineDataService(@Qualifier("marineDataWebClient") WebClient marineDataWebClient,
            MarineDataRepository marineDataRepository,
            DataSourceStatusService dataSourceStatusService,
            ObjectMapper objectMapper) {
        this.marineDataWebClient = marineDataWebClient;
        this.marineDataRepository = marineDataRepository;
        this.dataSourceStatusService = dataSourceStatusService;
        this.objectMapper = objectMapper;
    }

    public Mono<Void> fetchAndStoreMarineData() {
        logger.info("Starting marine data fetch for all default stations");

        return Mono.fromRunnable(() -> {
            for (String stationId : DEFAULT_MARINE_STATIONS) {
                fetchStationData(stationId)
                        .doOnSuccess(data -> logger.info("Successfully fetched marine data for station: {}", stationId))
                        .doOnError(
                                error -> logger.error("Failed to fetch marine data for station: {}", stationId, error))
                        .subscribe();
            }
        });
    }

    public Mono<MarineData> fetchStationData(String stationId) {
        String today = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return marineDataWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("station", stationId)
                        .queryParam("product", "water_level")
                        .queryParam("date", "latest")
                        .queryParam("datum", "MLLW")
                        .queryParam("units", "english")
                        .queryParam("time_zone", "gmt")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> parseMarineResponse(response, stationId))
                .doOnSuccess(marineData -> {
                    if (marineData != null) {
                        marineDataRepository.save(marineData);
                        dataSourceStatusService.recordSuccessfulFetch("marine-data");
                    }
                })
                .doOnError(error -> {
                    logger.error("Error fetching marine data for station {}: {}", stationId, error.getMessage());
                    dataSourceStatusService.recordError("marine-data", error.getMessage());
                });
    }

    private MarineData parseMarineResponse(JsonNode response, String stationId) {
        try {
            JsonNode data = response.get("data");
            if (data == null || !data.isArray() || data.size() == 0) {
                logger.warn("No data found in marine response for station: {}", stationId);
                return null;
            }

            // Get the most recent data point
            JsonNode latestData = data.get(0);

            MarineData marineData = new MarineData();
            marineData.setStationId(stationId);

            // Parse timestamp
            String timestampStr = latestData.get("t").asText();
            marineData.setTimestamp(OffsetDateTime.parse(timestampStr.replace(" ", "T") + "Z",
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME));

            // Parse water level
            JsonNode waterLevelNode = latestData.get("v");
            if (waterLevelNode != null && !waterLevelNode.isNull()) {
                marineData.setWaterLevel(new BigDecimal(waterLevelNode.asDouble()));
            }

            // Get station metadata for location
            JsonNode metadata = response.get("metadata");
            if (metadata != null) {
                JsonNode latNode = metadata.get("lat");
                JsonNode lonNode = metadata.get("lon");
                if (latNode != null && lonNode != null) {
                    marineData.setLatitude(new BigDecimal(latNode.asDouble()));
                    marineData.setLongitude(new BigDecimal(lonNode.asDouble()));
                }
            }

            // Store raw data
            marineData.setRawData(response);

            return marineData;

        } catch (Exception e) {
            logger.error("Error parsing marine response for station {}: {}", stationId, e.getMessage());
            return null;
        }
    }

    public List<MarineData> getRecentMarineData(int hours) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(hours);
        return marineDataRepository.findRecentMarineData(since);
    }

    public List<MarineData> getStationData(String stationId) {
        return marineDataRepository.findByStationIdOrderByTimestampDesc(stationId);
    }

    public List<String> getAvailableStations() {
        return marineDataRepository.findDistinctStationIds();
    }

    public MarineData getLatestStationData(String stationId) {
        return marineDataRepository.findLatestByStationId(stationId);
    }
}
