package io.polaris.sebrae.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CourseLessonCountRequestDTO {

    @NotNull
    @Min(1)
    private Integer totalLessons;

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }
}
