package io.polaris.sebrae;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task E-3: valida configurações de segurança críticas na inicialização.
 * O sistema não deve subir com configurações de segurança ausentes ou inseguras.
 */
@Component
public class ApplicationReadinessValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ApplicationReadinessValidator.class);

    private final String internalToken;
    private final String internalSourcesStr;
    private final String trustedProxyIpsStr;
    private final int burstThreshold;
    private final long burstWindowMs;
    private final int tokenFailureThreshold;
    private final boolean requireSource;

    public ApplicationReadinessValidator(
            @Value("${polaris.internal-token:}") String internalToken,
            @Value("${polaris.internal-sources:}") String internalSourcesStr,
            @Value("${polaris.trusted-proxy-ips:}") String trustedProxyIpsStr,
            @Value("${polaris.burst-detection.recalculate.threshold:5}") int burstThreshold,
            @Value("${polaris.burst-detection.recalculate.window-ms:60000}") long burstWindowMs,
            @Value("${polaris.security.token-failure-threshold:10}") int tokenFailureThreshold,
            @Value("${polaris.security.require-source:true}") boolean requireSource) {
        this.internalToken = internalToken;
        this.internalSourcesStr = internalSourcesStr;
        this.trustedProxyIpsStr = trustedProxyIpsStr;
        this.burstThreshold = burstThreshold;
        this.burstWindowMs = burstWindowMs;
        this.tokenFailureThreshold = tokenFailureThreshold;
        this.requireSource = requireSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateSecurityConfiguration();
    }

    private void validateSecurityConfiguration() {
        boolean failed = false;

        // 1. Token interno — já validado com fail-fast no InternalTokenAuthFilter/@PostConstruct
        //    Aqui apenas logamos o estado.
        if (internalToken == null || internalToken.isBlank()) {
            log.error("[SECURITY-BOOT] polaris.internal-token não está configurado. O sistema pode ter falhado antes chegar aqui.");
            failed = true;
        }

        // 2. internal-sources: sem mapeamento, todos os callers recebem ROLE_INTERNAL_SERVICE (acesso amplo)
        if (internalSourcesStr == null || internalSourcesStr.isBlank()) {
            if (requireSource) {
                log.error("[SECURITY-BOOT] polaris.internal-sources não configurado MAS polaris.security.require-source=true. " +
                        "Isso fará com que TODAS as requisições internas sejam bloqueadas com 401.");
                failed = true;
            } else {
                log.warn("[SECURITY-BOOT] polaris.internal-sources não configurado. " +
                        "Todos os callers autenticados receberão ROLE_INTERNAL_SERVICE sem separação de escopo. " +
                        "Recomendado configurar: collector:ROLE_COLLECTOR,analytics:ROLE_ANALYTICS,admin:ROLE_ADMIN");
            }
        } else {
            log.info("[SECURITY-BOOT] polaris.internal-sources configurado com {} entradas.", countEntries(internalSourcesStr));
        }

        // 3. trusted-proxy-ips: sem configuração, leitura de X-Forwarded-For está desabilitada (seguro por padrão)
        if (trustedProxyIpsStr == null || trustedProxyIpsStr.isBlank()) {
            log.info("[SECURITY-BOOT] polaris.trusted-proxy-ips não configurado. " +
                    "X-Forwarded-For será ignorado (comportamento seguro para ambientes sem proxy reverso).");
        }

        // 4. Burst threshold deve ser positivo e dentro de faixa razoável
        if (burstThreshold <= 0) {
            log.error("[SECURITY-BOOT] polaris.burst-detection.recalculate.threshold deve ser > 0. Valor atual: {}", burstThreshold);
            failed = true;
        } else if (burstThreshold > 100) {
            log.warn("[SECURITY-BOOT] polaris.burst-detection.recalculate.threshold={} é muito alto. " +
                    "Recomendado <= 20 para detectar abuso efetivamente.", burstThreshold);
        }

        // 5. Janela de burst deve ser positiva
        if (burstWindowMs <= 0) {
            log.error("[SECURITY-BOOT] polaris.burst-detection.recalculate.window-ms deve ser > 0. Valor atual: {}", burstWindowMs);
            failed = true;
        }

        // 6. Threshold de falha de token deve ser positivo
        if (tokenFailureThreshold <= 0) {
            log.error("[SECURITY-BOOT] polaris.security.token-failure-threshold deve ser > 0. Valor atual: {}", tokenFailureThreshold);
            failed = true;
        }

        if (failed) {
            throw new IllegalStateException(
                    "[SECURITY-BOOT] Configurações de segurança críticas ausentes ou inválidas. Verifique os logs acima.");
        }

        log.info("[SECURITY-BOOT] Validação de configurações de segurança concluída.");
    }

    private int countEntries(String csv) {
        if (csv == null || csv.isBlank()) return 0;
        return csv.split(",").length;
    }
}
