package io.polaris.sebrae.model;

import io.polaris.sebrae.model.enums.SignalWeightKey;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "signal_weight_configs")
public class SignalWeightConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal_key", unique = true, nullable = false, length = 40)
    private SignalWeightKey signalKey;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal weight;

    @Column(nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SignalWeightConfig() {}

    public SignalWeightConfig(SignalWeightKey signalKey, String label, BigDecimal weight, Boolean enabled) {
        this.signalKey = signalKey;
        this.label = label;
        this.weight = weight;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public SignalWeightKey getSignalKey() { return signalKey; }
    public void setSignalKey(SignalWeightKey signalKey) { this.signalKey = signalKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
