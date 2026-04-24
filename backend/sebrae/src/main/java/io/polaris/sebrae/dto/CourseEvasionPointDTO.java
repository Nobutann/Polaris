package io.polaris.sebrae.dto;

public class CourseEvasionPointDTO {
    private Long lessonId;
    private Long count;

    public CourseEvasionPointDTO() {}

    public CourseEvasionPointDTO(Long lessonId, Long count) {
        this.lessonId = lessonId;
        this.count = count;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
