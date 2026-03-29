package io.polaris.sebrae.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import io.polaris.sebrae.model.enums.EventType;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Long courseId;
    
    private Long lessonId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    private String device;
    private String browser;
    
    @CreationTimestamp
    private LocalDateTime timestamp;

    public Event() {}

    public Event(Long userId, Long courseId, Long lessonId, EventType type, String metadata, String device, String browser) {
        this.userId = userId;
        this.courseId = courseId;
        this.lessonId = lessonId;
        this.lessonId = lessonId;
        this.type = type;
        this.metadata = metadata;
        this.device = device;
        this.browser = browser;
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
    
    public EventType getType() {
    	return type;
    }
    
    public void setType(EventType type) {
    	this.type = type;
    }
    
    public String getMetadata() {
    	return metadata;
    }
    
    public void setMetadata(String metadata) {
    	this.metadata = metadata;
    }
    
    public String getDevice() {
    	return device;
    }
    
    public void setDevice(String device) {
    	this.device = device;
    }
    
    public String getBrowser() {
    	return browser;
    }
    
    public void setBrowser(String browser) {
    	this.browser = browser;
    }
    
    public LocalDateTime getTimestamp() {
    	return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
    	this.timestamp = timestamp;
    }
}