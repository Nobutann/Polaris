package io.polaris.sebrae.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public class UpdateSignalWeightRequestDTO {

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private java.math.BigDecimal weight;

    private Boolean enabled;

    public UpdateSignalWeightRequestDTO() {}

    public java.math.BigDecimal getWeight() { return weight; }
    public void setWeight(java.math.BigDecimal weight) { this.weight = weight; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    /** Retorna true se pelo menos um campo foi fornecido (validação de negócio). */
    public boolean hasAtLeastOneField() {
        return weight != null || enabled != null;
    }
}
