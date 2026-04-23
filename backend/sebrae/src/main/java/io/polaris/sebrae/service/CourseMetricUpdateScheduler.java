package io.polaris.sebrae.service;

import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseMetricUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(CourseMetricUpdateScheduler.class);

    private final EventRepository eventRepository;
    private final CourseMetricService courseMetricService;
    private final AbandonmentService abandonmentService;

    public CourseMetricUpdateScheduler(EventRepository eventRepository, CourseMetricService courseMetricService,
                                       AbandonmentService abandonmentService) {
        this.eventRepository = eventRepository;
        this.courseMetricService = courseMetricService;
        this.abandonmentService = abandonmentService;
    }

    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void recalculateAllMetrics() {
        logger.info("Starting hourly course metric recalculation job.");
        List<Object[]> userCoursePairs = eventRepository.findDistinctUserCourseWithRelevantActivity(EventType.RELEVANT_ACTIVITY_TYPES);
        
        int count = 0;
        for (Object[] pair : userCoursePairs) {
            try {
                Long userId = ((Number) pair[0]).longValue();
                Long courseId = ((Number) pair[1]).longValue();
                courseMetricService.recalculate(userId, courseId);
                abandonmentService.evaluate(userId, courseId);
                count++;
            } catch (Exception e) {
                logger.error("Error recalculating metrics for user-course pair: " + pair[0] + "-" + pair[1], e);
            }
        }
        logger.info("Finished hourly course metric recalculation. Processed {} pairs.", count);
    }
}
