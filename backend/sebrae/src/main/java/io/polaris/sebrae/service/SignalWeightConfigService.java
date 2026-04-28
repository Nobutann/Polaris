package io.polaris.sebrae.service;

import io.polaris.sebrae.dto.SignalWeightConfigDTO;
import io.polaris.sebrae.dto.UpdateSignalWeightRequestDTO;
import io.polaris.sebrae.model.SignalWeightConfig;
import io.polaris.sebrae.model.enums.SignalWeightKey;
import io.polaris.sebrae.repository.SignalWeightConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SignalWeightConfigService {

    private static final Logger log = LoggerFactory.getLogger(SignalWeightConfigService.class);

    /** Pesos padrão — fallback quando o banco estiver vazio ou inacessível. */
    static final Map<SignalWeightKey, BigDecimal> DEFAULT_WEIGHTS = new EnumMap<>(SignalWeightKey.class);
    static {
        DEFAULT_WEIGHTS.put(SignalWeightKey.INACTIVITY,    new BigDecimal("0.4000"));
        DEFAULT_WEIGHTS.put(SignalWeightKey.CONTINUITY,    new BigDecimal("0.2500"));
        DEFAULT_WEIGHTS.put(SignalWeightKey.COMPLETION,    new BigDecimal("0.2000"));
        DEFAULT_WEIGHTS.put(SignalWeightKey.ADVANCE_DEPTH, new BigDecimal("0.1500"));
    }

    /** Labels padrão usadas no reset/upsert. */
    private static final Map<SignalWeightKey, String> DEFAULT_LABELS = new EnumMap<>(SignalWeightKey.class);
    static {
        DEFAULT_LABELS.put(SignalWeightKey.INACTIVITY,    "Inatividade do aluno");
        DEFAULT_LABELS.put(SignalWeightKey.CONTINUITY,    "Taxa de continuidade");
        DEFAULT_LABELS.put(SignalWeightKey.COMPLETION,    "Taxa de conclusão");
        DEFAULT_LABELS.put(SignalWeightKey.ADVANCE_DEPTH, "Profundidade de avanço");
    }

    private final SignalWeightConfigRepository repository;

    public SignalWeightConfigService(SignalWeightConfigRepository repository) {
        this.repository = repository;
    }

    /** Retorna todos os sinais cadastrados como DTOs. */
    @Transactional
    public List<SignalWeightConfigDTO> getAll() {
        ensureDefaultsExist();
        return repository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualização parcial: atualiza {@code weight} e/ou {@code enabled} do sinal indicado.
     *
     * @throws IllegalArgumentException se signalKey não existir no banco.
     * @throws IllegalStateException    se a operação deixaria todos os sinais desativados.
     */
    @Transactional
    public SignalWeightConfigDTO update(SignalWeightKey key, UpdateSignalWeightRequestDTO dto) {
        ensureDefaultsExist();

        if (dto.getWeight() != null) {
            if (dto.getWeight().compareTo(BigDecimal.ZERO) < 0 || dto.getWeight().compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("Weight must be between 0.0 and 1.0.");
            }
        }

        SignalWeightConfig config = repository.findBySignalKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Signal key not found: " + key));

        // Guard: pelo menos um campo deve ser fornecido
        if (!dto.hasAtLeastOneField()) {
            throw new IllegalArgumentException("At least one field (weight or enabled) must be provided.");
        }

        // Guard: não pode desativar o último sinal ativo
        if (Boolean.FALSE.equals(dto.getEnabled())) {
            long currentlyEnabled = repository.findAll().stream()
                    .filter(c -> !c.getSignalKey().equals(key))
                    .filter(c -> Boolean.TRUE.equals(c.getEnabled()))
                    .count();
            if (currentlyEnabled == 0) {
                throw new IllegalStateException("Cannot disable the last active signal.");
            }
        }

        if (dto.getWeight() != null) config.setWeight(dto.getWeight());
        if (dto.getEnabled() != null) config.setEnabled(dto.getEnabled());

        return toDTO(repository.save(config));
    }

    /**
     * Upsert: restaura / recria todos os 4 sinais com seus valores padrão.
     * Sinais já existentes são atualizados; ausentes são criados.
     */
    @Transactional
    public List<SignalWeightConfigDTO> reset() {
        for (SignalWeightKey key : SignalWeightKey.values()) {
            SignalWeightConfig config = repository.findBySignalKey(key)
                    .orElseGet(() -> new SignalWeightConfig(key, DEFAULT_LABELS.get(key), DEFAULT_WEIGHTS.get(key), true));
            config.setWeight(DEFAULT_WEIGHTS.get(key));
            config.setEnabled(true);
            repository.save(config);
        }
        return getAll();
    }

    /**
     * Retorna mapa de pesos apenas dos sinais com {@code enabled=true}.
     * Se o resultado for vazio (banco vazio ou todos desativados), retorna os pesos padrão como fallback.
     */
    @Transactional
    public Map<SignalWeightKey, BigDecimal> getActiveWeightMap() {
        try {
            ensureDefaultsExist();
            Map<SignalWeightKey, BigDecimal> map = repository.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getEnabled()))
                    .collect(Collectors.toMap(
                            SignalWeightConfig::getSignalKey,
                            SignalWeightConfig::getWeight,
                            (a, b) -> a,
                            () -> new EnumMap<>(SignalWeightKey.class)
                    ));

            if (map.isEmpty()) {
                log.warn("No active signal weights found in DB — using hardcoded defaults.");
                return new EnumMap<>(DEFAULT_WEIGHTS);
            }
            return map;
        } catch (Exception e) {
            log.warn("Failed to load signal weights from DB — falling back to defaults.", e);
            return new EnumMap<>(DEFAULT_WEIGHTS);
        }
    }

    // -----------------------------------------------------------------------
    private void ensureDefaultsExist() {
        for (SignalWeightKey key : SignalWeightKey.values()) {
            if (repository.findBySignalKey(key).isEmpty()) {
                repository.save(new SignalWeightConfig(key, DEFAULT_LABELS.get(key), DEFAULT_WEIGHTS.get(key), true));
            }
        }
    }

    private SignalWeightConfigDTO toDTO(SignalWeightConfig c) {
        return new SignalWeightConfigDTO(c.getId(), c.getSignalKey(), c.getLabel(),
                c.getWeight(), c.getEnabled(), c.getUpdatedAt());
    }
}
