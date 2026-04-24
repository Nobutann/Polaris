package io.polaris.sebrae.config;

import io.polaris.sebrae.service.AuditLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Detecta burst de operações administrativas por IP em janela de tempo.
 * Quando um mesmo IP ultrapassa o threshold de operações na janela configurada,
 * um alerta de BURST_DETECTED é registrado no AuditLogger.
 */
@Component
public class BurstDetector {

    private final AuditLogger auditLogger;
    private final int threshold;
    private final long windowMs;

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> windowStart = new ConcurrentHashMap<>();

    public BurstDetector(
            AuditLogger auditLogger,
            @Value("${polaris.burst-detection.recalculate.threshold:5}") int threshold,
            @Value("${polaris.burst-detection.recalculate.window-ms:60000}") long windowMs) {
        this.auditLogger = auditLogger;
        this.threshold = threshold;
        this.windowMs = windowMs;
    }

    /**
     * Registra uma operação para o IP/source fornecido e verifica burst.
     *
     * @param ip    IP do caller
     * @param source source declarado (X-Internal-Source ou "unknown")
     * @param route rota que está sendo chamada
     */
    public void record(String ip, String source, String route) {
        if (ip == null) {
            ip = "unknown";
        }
        final String key = ip;
        long now = System.currentTimeMillis();

        // Se a janela expirou, reinicia o contador
        windowStart.compute(key, (k, ts) -> {
            if (ts == null || (now - ts) > windowMs) {
                counters.put(key, new AtomicInteger(0));
                return now;
            }
            return ts;
        });

        int count = counters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();

        if (count >= threshold) {
            long windowSeconds = windowMs / 1000;
            auditLogger.logAdminOperationBurst(source != null ? source : "unknown", route, count, windowSeconds);
        }
    }
}
