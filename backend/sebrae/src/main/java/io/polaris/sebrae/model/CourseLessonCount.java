package io.polaris.sebrae.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_lesson_counts")
public class CourseLessonCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, unique = true)
    private Long courseId;

    @Column(name = "total_lessons", nullable = false)
    private Integer totalLessons;

    public CourseLessonCount() {}

    public CourseLessonCount(Long courseId, Integer totalLessons) {
        this.courseId = courseId;
        this.totalLessons = totalLessons;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }
}
