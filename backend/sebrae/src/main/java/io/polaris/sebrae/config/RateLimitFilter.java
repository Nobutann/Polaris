package io.polaris.sebrae.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.polaris.sebrae.service.AuditLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> collectionBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> analyticsBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> adminBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();

    private final int collectionLimit;
    private final int analyticsLimit;
    private final int adminLimit;
    private final int defaultLimit;

    private final AuditLogger auditLogger;

    public RateLimitFilter(
            @Value("${rate-limit.collection.requests-per-minute:60}") int collectionLimit,
            @Value("${rate-limit.analytics.requests-per-minute:30}") int analyticsLimit,
            @Value("${rate-limit.admin.requests-per-minute:10}") int adminLimit,
            @Value("${rate-limit.default.requests-per-minute:120}") int defaultLimit,
            AuditLogger auditLogger) {
        this.collectionLimit = collectionLimit;
        this.analyticsLimit = analyticsLimit;
        this.adminLimit = adminLimit;
        this.defaultLimit = defaultLimit;
        this.auditLogger = auditLogger;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = auditLogger.getClientIP();
        Bucket bucket;
        int limit;

        if (path.startsWith("/api/events") || path.startsWith("/api/sessions")) {
            bucket = collectionBuckets.computeIfAbsent(ip, k -> createNewBucket(collectionLimit));
            limit = collectionLimit;
        } else if (path.startsWith("/api/metrics/courses")) {
            if (path.contains("/recalculate")) {
                bucket = adminBuckets.computeIfAbsent(ip, k -> createNewBucket(adminLimit));
                limit = adminLimit;
            } else {
                bucket = analyticsBuckets.computeIfAbsent(ip, k -> createNewBucket(analyticsLimit));
                limit = analyticsLimit;
            }
        } else if (path.equals("/api/signals/ingest/youtube") || path.contains("/lesson-count")) {
            bucket = adminBuckets.computeIfAbsent(ip, k -> createNewBucket(adminLimit));
            limit = adminLimit;
        } else if (path.startsWith("/api/signals")) {
            if (request.getMethod().equalsIgnoreCase("GET")) {
                bucket = analyticsBuckets.computeIfAbsent(ip, k -> createNewBucket(analyticsLimit));
                limit = analyticsLimit;
            } else {
                bucket = collectionBuckets.computeIfAbsent(ip, k -> createNewBucket(collectionLimit));
                limit = collectionLimit;
            }
        } else {
            bucket = defaultBuckets.computeIfAbsent(ip, k -> createNewBucket(defaultLimit));
            limit = defaultLimit;
        }

        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);
        } else {
            auditLogger.logRateLimitExceeded(ip, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Too many requests");
        }
    }

    private Bucket createNewBucket(int capacity) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
