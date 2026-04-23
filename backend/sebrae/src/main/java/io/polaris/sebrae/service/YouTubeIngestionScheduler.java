package io.polaris.sebrae.service;

import io.polaris.sebrae.service.collector.YouTubeSignalCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class YouTubeIngestionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(YouTubeIngestionScheduler.class);

    private final ExternalSignalIngestionService externalSignalIngestionService;
    private final YouTubeSignalCollector youTubeSignalCollector;

    public YouTubeIngestionScheduler(ExternalSignalIngestionService externalSignalIngestionService, YouTubeSignalCollector youTubeSignalCollector) {
        this.externalSignalIngestionService = externalSignalIngestionService;
        this.youTubeSignalCollector = youTubeSignalCollector;
    }

    @Scheduled(fixedDelayString = "${youtube.ingestion.delay-ms:3600000}")
    public void ingestYouTubeComments() {
        logger.info("Starting YouTube comments ingestion task.");
        try {
            externalSignalIngestionService.ingest(youTubeSignalCollector);
            logger.info("Finished YouTube comments ingestion task.");
        } catch (Exception e) {
            logger.error("Error during YouTube comments ingestion task", e);
        }
    }
}
