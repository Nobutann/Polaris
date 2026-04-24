package io.polaris.sebrae.config;

import io.polaris.sebrae.service.AuditLogger;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class InternalTokenAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalTokenAuthFilter.class);

    private final String internalToken;
    private final Map<String, String> internalSources;
    private final int tokenFailureThreshold;
    private final long tokenFailureWindowMs;
    private final AuditLogger auditLogger;

    private final Map<String, AtomicInteger> tokenFailuresCounters = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenFailuresTimestamps = new ConcurrentHashMap<>();
    private final boolean requireSource;

    public InternalTokenAuthFilter(
        @Value("${polaris.internal-token:}") String internalToken, 
        @Value("${polaris.internal-sources:}") String internalSourcesStr,
        @Value("${polaris.security.token-failure-threshold:10}") int tokenFailureThreshold,
        @Value("${polaris.security.token-failure-windowMs:300000}") long tokenFailureWindowMs,
        @Value("${polaris.security.require-source:true}") boolean requireSource,
        AuditLogger auditLogger) {
        
        this.internalToken = internalToken;
        this.tokenFailureThreshold = tokenFailureThreshold;
        this.tokenFailureWindowMs = tokenFailureWindowMs;
        this.requireSource = requireSource;
        this.auditLogger = auditLogger;
        this.internalSources = parseSources(internalSourcesStr);
    }

    private Map<String, String> parseSources(String str) {
        Map<String, String> map = new HashMap<>();
        if (str != null && !str.trim().isEmpty()) {
            for (String pair : str.split(",")) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        return map;
    }

    @PostConstruct
    public void validateConfig() {
        Assert.hasText(internalToken, "polaris.internal-token deve ser configurado");
        Assert.isTrue(!internalToken.equals("default-dev-token"), "polaris.internal-token não pode ser o valor default no ambiente de produção");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String tokenHeader = request.getHeader("X-Internal-Token");
        String sourceHeader = request.getHeader("X-Internal-Source");

        if (tokenHeader != null) {
            if (isEqualConstantTime(tokenHeader, internalToken)) {
                String role = "ROLE_INTERNAL_SERVICE";
                if (sourceHeader != null && internalSources.containsKey(sourceHeader)) {
                    role = internalSources.get(sourceHeader);
                } else {
                    if (requireSource) {
                        String failReason = (sourceHeader == null) ? "SOURCE_HEADER_MISSING" : "SOURCE_VALUE_UNMAPPED";
                        auditLogger.logAuthFailure(request.getRemoteAddr(), request.getRequestURI(), failReason);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized - Invalid Internal Source\"}");
                        return;
                    } else {
                        logger.warn("Source {} not mapped or missing from IP {} for URI {}, defaulting to ROLE_INTERNAL_SERVICE", sourceHeader, request.getRemoteAddr(), request.getRequestURI());
                    }
                }
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "internal-service", null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                handleTokenFailure(request.getRemoteAddr());
                auditLogger.logAuthFailure(null, request.getRequestURI(), "AUTH_TOKEN_INVALID");
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleTokenFailure(String ip) {
        if (ip == null) return;
        long now = System.currentTimeMillis();
        
        tokenFailuresTimestamps.compute(ip, (k, ts) -> {
            if (ts == null || (now - ts) > tokenFailureWindowMs) {
                tokenFailuresCounters.put(ip, new AtomicInteger(0));
                return now;
            }
            return ts;
        });
        
        int failures = tokenFailuresCounters.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();
        if (failures >= tokenFailureThreshold && failures % tokenFailureThreshold == 0) {
            auditLogger.logBruteForceSuspected(ip, failures, tokenFailureWindowMs / 1000);
        }
    }

    private boolean isEqualConstantTime(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }
}
