package io.polaris.sebrae.service;

import io.polaris.sebrae.dto.CourseEvasionPointDTO;
import io.polaris.sebrae.dto.CourseAggregateSummaryDTO;
import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;
import io.polaris.sebrae.service.AbandonmentService.AbandonmentStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseMetricQueryService {

    private final CourseMetricSnapshotRepository snapshotRepository;
    private final CourseMetricService courseMetricService;
    private final AbandonmentService abandonmentService;

    public CourseMetricQueryService(CourseMetricSnapshotRepository snapshotRepository,
                                    CourseMetricService courseMetricService,
                                    AbandonmentService abandonmentService) {
        this.snapshotRepository = snapshotRepository;
        this.courseMetricService = courseMetricService;
        this.abandonmentService = abandonmentService;
    }

    public Page<CourseAggregateSummaryDTO> getByCourse(Long courseId, Pageable pageable) {
        return snapshotRepository.findByCourseId(courseId, pageable)
                .map(this::toSummaryDTO);
    }

    public List<CourseEvasionPointDTO> getEvasionPoints(Long courseId) {
        Map<Long, Long> evasionCounts = courseMetricService.findEvasionPointsByCourse(courseId);
        return evasionCounts.entrySet().stream()
                .map(e -> new CourseEvasionPointDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public Page<CourseAggregateSummaryDTO> getAll(Pageable pageable) {
        return snapshotRepository.findAll(pageable)
                .map(this::toSummaryDTO);
    }

    private CourseAggregateSummaryDTO toSummaryDTO(CourseMetricSnapshot snapshot) {
        CourseAggregateSummaryDTO dto = new CourseAggregateSummaryDTO();
        dto.setUserId(snapshot.getUserId());
        dto.setCourseId(snapshot.getCourseId());
        dto.setDaysSinceLastActivity(snapshot.getDaysSinceLastActivity());
        dto.setRiskBand(snapshot.getRiskBand());
        dto.setReturnFrequency30d(snapshot.getReturnFrequency30d());
        dto.setContinuityRate(snapshot.getContinuityRate());
        dto.setLastRelevantActivityAt(snapshot.getLastRelevantActivityAt());
        dto.setCalculatedAt(snapshot.getCalculatedAt());
        
        dto.setCompletionRatio(snapshot.getCompletionRatio());
        dto.setRetained7d(snapshot.getRetained7d());
        dto.setRetained14d(snapshot.getRetained14d());
        dto.setRetained30d(snapshot.getRetained30d());
        dto.setFirstRelevantActivityAt(snapshot.getFirstRelevantActivityAt());
        dto.setAdvanceDepth(snapshot.getAdvanceDepth());

        // --- Abandonment domain fields (derived from persisted events) ---
        AbandonmentStatus status = abandonmentService.getStatus(snapshot.getUserId(), snapshot.getCourseId());
        dto.setAbandoned(status.isAbandoned());
        dto.setAbandonedAt(status.getAbandonedAt());
        dto.setReturnedAfterAbandonment(status.isReturnedAfterAbandonment());
        dto.setReturnedAt(status.getReturnedAt());
        dto.setAbandonmentStatus(status.getAbandonmentStatus());

        dto.setWeightedRiskScore(snapshot.getWeightedRiskScore());
        dto.setPriorityLevel(snapshot.getPriorityLevel() != null
            ? snapshot.getPriorityLevel().name().toLowerCase() : null);
        dto.setMainRiskReason(snapshot.getMainRiskReason() != null
            ? snapshot.getMainRiskReason().name().toLowerCase() : null);

        return dto;
    }
}
