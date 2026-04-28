package io.polaris.sebrae.config;

import io.polaris.sebrae.security.JwtAuthFilter;
import io.polaris.sebrae.service.AuditLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final InternalTokenAuthFilter internalTokenAuthFilter;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuditLogger auditLogger;

    public SecurityConfig(InternalTokenAuthFilter internalTokenAuthFilter, JwtAuthFilter jwtAuthFilter, AuditLogger auditLogger) {
        this.internalTokenAuthFilter = internalTokenAuthFilter;
        this.jwtAuthFilter = jwtAuthFilter;
        this.auditLogger = auditLogger;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
    		return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        );

        http.headers(headers -> headers
            .frameOptions(frame -> frame.deny())
            .xssProtection(xss -> xss.disable())
            .contentTypeOptions(Customizer.withDefaults())
            .cacheControl(Customizer.withDefaults())
            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/api/auth/login"
                ).permitAll()

                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/metrics/courses/*/recalculate").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/metrics/courses/**").hasAnyRole("ANALYTICS", "ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/abandonment/**").hasAnyRole("ANALYTICS", "ADMIN")
                .requestMatchers("/api/courses/*/lesson-count").hasRole("ADMIN")
                .requestMatchers("/api/signals/ingest/youtube").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/events/**", "/api/sessions/**", "/api/signals").hasRole("COLLECTOR")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/signals").hasAnyRole("ANALYTICS", "ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );
        
        // Pra qualquer pessoa que mexer aqui, o jwtauthfilter SEMPRE antes do internaltoken, obrigado.
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(internalTokenAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint((request, response, authException) -> {
                auditLogger.logAuthFailure(null, request.getRequestURI(), "AUTH_TOKEN_MISSING");
                response.setStatus(org.springframework.http.HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Unauthorized\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                String principal = null;
                if (request.getUserPrincipal() != null) {
                    principal = request.getUserPrincipal().getName();
                }
                auditLogger.logAccessDenied(null, request.getRequestURI(), principal);
                response.setStatus(org.springframework.http.HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden\"}");
            })
        );

        return http.build();
    }
}
