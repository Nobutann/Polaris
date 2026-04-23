package io.polaris.sebrae.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
public class AuditLogger {

    private static final Logger logger = LoggerFactory.getLogger("audit");
    private final List<String> trustedProxyIps;

    public AuditLogger(@Value("${polaris.trusted-proxy-ips:}") String trustedProxyIpsStr) {
        if (trustedProxyIpsStr == null || trustedProxyIpsStr.trim().isEmpty()) {
            this.trustedProxyIps = Collections.emptyList();
        } else {
            this.trustedProxyIps = Arrays.asList(trustedProxyIpsStr.split(","));
        }
    }

    public void logAdminAction(String action, String endpoint, Map<String, Object> context) {
        logger.info("[AUDIT] admin action", 
                kv("action", action),
                kv("endpoint", endpoint != null ? endpoint : "unknown"),
                kv("ip", getClientIP()),
                kv("context", context),
                kv("at", now()));
    }

    public void logAuthFailure(String ip, String route, String reason) {
        logger.warn("[AUDIT] auth failure",
                kv("action", "AUTH_FAILURE"),
                kv("endpoint", route),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("reason", reason),
                kv("at", now()));
    }

    public void logAccessDenied(String ip, String route, String principal) {
        logger.warn("[AUDIT] access denied",
                kv("action", "ACCESS_DENIED"),
                kv("endpoint", route),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("principal", principal != null ? principal : "anonymous"),
                kv("at", now()));
    }

    public void logRateLimitExceeded(String ip, String route) {
        logger.warn("[AUDIT] rate limit exceeded",
                kv("action", "RATE_LIMIT_EXCEEDED"),
                kv("endpoint", route),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("at", now()));
    }

    public void logSessionAbuseAttempt(String ip, Long sessionId, Long requestedUserId, Long actualUserId) {
        logger.warn("[AUDIT] session abuse attempt",
                kv("action", "SESSION_ABUSE_ATTEMPT"),
                kv("sessionId", sessionId),
                kv("requestedUserId", requestedUserId),
                kv("actualUserId", actualUserId),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("at", now()));
    }

    public void logInvalidPayload(String ip, String route, String field, String reason) {
        logger.warn("[AUDIT] invalid payload",
                kv("action", "INVALID_PAYLOAD"),
                kv("endpoint", route),
                kv("field", field),
                kv("reason", reason),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("at", now()));
    }

    public void logInvalidIdReference(String ip, String route, String field, Object value) {
        logger.warn("[AUDIT] invalid id reference",
                kv("action", "INVALID_ID_REFERENCE"),
                kv("endpoint", route),
                kv("field", field),
                kv("value", value),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("at", now()));
    }

    public void logAdminOperationBurst(String source, String route, int count, long windowSeconds) {
        logger.error("[AUDIT] burst detected",
                kv("action", "BURST_DETECTED"),
                kv("source", source),
                kv("endpoint", route),
                kv("count", count),
                kv("windowSeconds", windowSeconds),
                kv("ip", getClientIP()),
                kv("at", now()));
    }

    public void logBruteForceSuspected(String ip, int count, long windowSeconds) {
        logger.error("[AUDIT] token brute force suspected",
                kv("action", "BRUTE_FORCE_SUSPECTED"),
                kv("count", count),
                kv("windowSeconds", windowSeconds),
                kv("ip", ip != null ? ip : getClientIP()),
                kv("at", now()));
    }

    public String getClientIP() {
        try {
            org.springframework.web.context.request.RequestAttributes attribs = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attribs instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) attribs).getRequest();
                
                String remoteAddr = request.getRemoteAddr();
                if (trustedProxyIps.contains(remoteAddr)) {
                    String xfHeader = request.getHeader("X-Forwarded-For");
                    if (xfHeader != null && !xfHeader.isEmpty()) {
                        return xfHeader.split(",")[0].trim();
                    }
                }
                return remoteAddr;
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "internal";
    }

    private String now() {
        return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
    }
}
