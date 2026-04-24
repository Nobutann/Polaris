package io.polaris.sebrae;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationReadinessValidatorTest {

    @Test
    public void shouldFailWhenRequireSourceIsTrueAndSourcesIsEmpty() {
        ApplicationReadinessValidator validator = new ApplicationReadinessValidator(
            "token",
            "", // internalSourcesStr vazio
            "", // trustedProxyIpsStr
            5,  // burstThreshold
            60000, // burstWindowMs
            10, // tokenFailureThreshold
            true // requireSource = true
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            validator.run(new DefaultApplicationArguments());
        });

        assertTrue(exception.getMessage().contains("Configurações de segurança críticas ausentes ou inválidas"));
    }

    @Test
    public void shouldNotFailWhenRequireSourceIsFalseAndSourcesIsEmpty() {
        ApplicationReadinessValidator validator = new ApplicationReadinessValidator(
            "token",
            "", // internalSourcesStr vazio
            "", // trustedProxyIpsStr
            5,  // burstThreshold
            60000, // burstWindowMs
            10, // tokenFailureThreshold
            false // requireSource = false
        );

        assertDoesNotThrow(() -> {
            validator.run(new DefaultApplicationArguments());
        });
    }
}
