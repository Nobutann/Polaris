package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;

import java.time.LocalDateTime;

public class SignalAckDTO {

    private Long id;
    private SignalSource source;
    private String type;
    private LocalDateTime collectedAt;

    public SignalAckDTO(Signal signal) {
        this.id = signal.getId();
        this.source = signal.getSource();
        this.type = signal.getType();
        this.collectedAt = signal.getCollectedAt();
    }

    public Long getId() {
        return id;
    }

    public SignalSource getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }
}
