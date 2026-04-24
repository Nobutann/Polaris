package io.polaris.sebrae.service;

import io.polaris.sebrae.model.CourseLessonCount;
import io.polaris.sebrae.repository.CourseLessonCountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CourseLessonCountService {

    private final CourseLessonCountRepository repository;
    private final AuditLogger auditLogger;

    public CourseLessonCountService(CourseLessonCountRepository repository, AuditLogger auditLogger) {
        this.repository = repository;
        this.auditLogger = auditLogger;
    }

    @Transactional
    public void saveOrUpdate(Long courseId, Integer totalLessons) {
        CourseLessonCount count = repository.findByCourseId(courseId)
                .orElse(new CourseLessonCount());
        
        count.setCourseId(courseId);
        count.setTotalLessons(totalLessons);
        
        repository.save(count);
        auditLogger.logAdminAction("SAVE_LESSON_COUNT", "/api/courses/" + courseId + "/lesson-count", Map.of("courseId", courseId, "totalLessons", totalLessons));
    }

    public Integer getTotalLessons(Long courseId) {
        return repository.findByCourseId(courseId)
                .map(CourseLessonCount::getTotalLessons)
                .orElse(null);
    }
}
