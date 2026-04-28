package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.enums.SignalWeightKey;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SignalWeightConfigDTO {

    private Long id;
    private SignalWeightKey signalKey;
    private String label;
    private BigDecimal weight;
    private Boolean enabled;
    private LocalDateTime updatedAt;

    public SignalWeightConfigDTO() {}

    public SignalWeightConfigDTO(Long id, SignalWeightKey signalKey, String label,
                                  BigDecimal weight, Boolean enabled, LocalDateTime updatedAt) {
        this.id = id;
        this.signalKey = signalKey;
        this.label = label;
        this.weight = weight;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public SignalWeightKey getSignalKey() { return signalKey; }
    public String getLabel() { return label; }
    public BigDecimal getWeight() { return weight; }
    public Boolean getEnabled() { return enabled; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
