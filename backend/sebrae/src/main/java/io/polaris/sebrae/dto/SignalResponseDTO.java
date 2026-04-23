package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;

import java.time.LocalDateTime;

public class SignalResponseDTO {

    private Long id;
    private SignalSource source;
    private String type;
    private Long userId;
    private Long courseId;
    private Long lessonId;
    private String externalId;
    private String externalUrl;
    private LocalDateTime collectedAt;

    public SignalResponseDTO(Signal signal) {
        this.id = signal.getId();
        this.source = signal.getSource();
        this.type = signal.getType();
        this.userId = signal.getUserId();
        this.courseId = signal.getCourseId();
        this.lessonId = signal.getLessonId();
        this.externalId = signal.getExternalId();
        this.externalUrl = signal.getExternalUrl();
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

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }
}
