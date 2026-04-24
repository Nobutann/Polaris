package io.polaris.sebrae.service;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.service.collector.ExternalSignalCollector;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExternalSignalIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalSignalIngestionService.class);
    private final SignalService signalService;
    private final AuditLogger auditLogger;

    public ExternalSignalIngestionService(SignalService signalService, AuditLogger auditLogger) {
        this.signalService = signalService;
        this.auditLogger = auditLogger;
    }

    public int ingest(ExternalSignalCollector collector) {
        List<Signal> signals = collector.collect();
        int savedCount = 0;
        for (Signal signal : signals) {
            try {
                signalService.save(signal);
                savedCount++;
            } catch (DataIntegrityViolationException e) {
                logger.warn("Signal já existe e foi ignorado na ingestão: externalId={}", signal.getExternalId());
            }
        }
        String collectorName = collector.getClass().getSimpleName();
        auditLogger.logAdminAction("EXTERNAL_SIGNAL_INGESTION", "/api/signals/ingest/" + collectorName.toLowerCase(), Map.of("collector", collectorName, "count", savedCount));
        return savedCount;
    }
}
