package io.polaris.sebrae.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_register")
public class ActivityRegister {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long lessonId;

    private String status;

    private LocalDateTime dateTimeRegister;

    public ActivityRegister() {}

    public ActivityRegister(Long userId, Long lessonId, String status) {
        this.userId = userId;
        this.lessonId = lessonId;
        this.status = status;
        this.dateTimeRegister = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDateTimeRegister() {
        return dateTimeRegister;
    }

    public void setDateTimeRegister(LocalDateTime dateTimeRegister) {
        this.dateTimeRegister = dateTimeRegister;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
