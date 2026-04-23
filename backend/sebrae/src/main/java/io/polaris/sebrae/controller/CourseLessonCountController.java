package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.CourseLessonCountRequestDTO;
import io.polaris.sebrae.service.CourseLessonCountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseLessonCountController {

    private final CourseLessonCountService service;

    public CourseLessonCountController(CourseLessonCountService service) {
        this.service = service;
    }

    @PostMapping("/{courseId}/lesson-count")
    public ResponseEntity<Void> registerLessonCount(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseLessonCountRequestDTO dto) {
            
        service.saveOrUpdate(courseId, dto.getTotalLessons());
        return ResponseEntity.ok().build();
    }
}
