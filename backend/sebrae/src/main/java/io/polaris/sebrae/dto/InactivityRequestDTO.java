package io.polaris.sebrae.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public class InactivityRequestDTO {

    @NotNull(message = "userId está faltando")
    @Min(1)
    @Max(100000000)
    private Long userId;
    
    @Positive
    @Max(100000000)
    private Long lessonId;
    
    @NotNull(message = "courseId está faltando")
    @Min(1)
    @Max(100000000)
    private Long courseId;
    
    @Size(max = 100)
    @jakarta.validation.constraints.Pattern(regexp = "^[\\x20-\\x7E]*$", message = "Caracteres de controle não são permitidos em device")
    private String device;
    
    @Size(max = 100)
    @jakarta.validation.constraints.Pattern(regexp = "^[\\x20-\\x7E]*$", message = "Caracteres de controle não são permitidos em browser")
    private String browser;

    public InactivityRequestDTO() {}

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
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
}
