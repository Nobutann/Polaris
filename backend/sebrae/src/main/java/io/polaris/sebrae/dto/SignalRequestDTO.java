package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.enums.SignalSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;

public class SignalRequestDTO {

    @NotNull(message = "source is required")
    private SignalSource source;

    @NotBlank(message = "type is required")
    @Size(max = 50)
    private String type;

    @Positive
    private Long userId;
    
    @Positive
    private Long courseId;
    
    @Positive
    private Long lessonId;
    
    @Size(max = 4096)
    private String content;
    
    @Size(max = 2048)
    private String metadata;

    public SignalRequestDTO() {}

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
}
