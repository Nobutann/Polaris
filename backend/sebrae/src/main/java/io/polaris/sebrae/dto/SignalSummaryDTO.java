package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import java.time.LocalDateTime;

public class SignalSummaryDTO {

    private SignalSource source;
    private String type;
    private Long courseId;
    private LocalDateTime collectedAt;

    public SignalSummaryDTO(Signal signal) {
        this.source = signal.getSource();
        this.type = signal.getType();
        this.courseId = signal.getCourseId();
        this.collectedAt = signal.getCollectedAt();
    }

    public SignalSource getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public Long getCourseId() {
        return courseId;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }
}
