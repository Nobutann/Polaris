package io.polaris.sebrae.model;

import io.polaris.sebrae.model.enums.SignalSource;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "signals")
public class Signal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalSource source;

    @Column(nullable = false, length = 100)
    private String type;

    private Long userId;

    private Long courseId;

    private Long lessonId;

    @Column(length = 255)
    private String externalId;

    @Column(columnDefinition = "TEXT")
    private String externalUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime collectedAt;

    public Signal() {}

    public Signal(
        SignalSource source,
        String type,
        Long userId,
        Long courseId,
        Long lessonId,
        String externalId,
        String externalUrl,
        String content,
        String metadata
    ) {
        this.source = source;
        this.type = type;
        this.userId = userId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.externalId = externalId;
        this.externalUrl = externalUrl;
        this.content = content;
        this.metadata = metadata;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SignalSource getSource() {
        return source;
    }

    public void setSource(SignalSource source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(LocalDateTime collectedAt) {
        this.collectedAt = collectedAt;
    }
}
