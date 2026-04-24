package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.AbandonmentStatusDTO;
import io.polaris.sebrae.model.CourseMetricSnapshot;
import io.polaris.sebrae.repository.CourseMetricSnapshotRepository;
import io.polaris.sebrae.service.AbandonmentService;
import io.polaris.sebrae.service.AbandonmentService.AbandonmentStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Endpoints for querying formal abandonment domain events.
 *
 * <p>Separate from the analytical metrics controller to preserve a clear
 * architectural boundary between domain events and risk-band inference.
 *
 * <p>All responses are derived from persisted COURSE_ABANDONED / COURSE_RETURNED events.
 */
@RestController
@RequestMapping("/api/abandonment")
public class AbandonmentController {

    private final AbandonmentService abandonmentService;
    private final CourseMetricSnapshotRepository snapshotRepository;

    public AbandonmentController(AbandonmentService abandonmentService,
                                 CourseMetricSnapshotRepository snapshotRepository) {
        this.abandonmentService = abandonmentService;
        this.snapshotRepository = snapshotRepository;
    }

    /**
     * GET /api/abandonment/courses/{courseId}/users/{userId}
     * Returns the formal abandonment status for a specific user/course pair.
     */
    @GetMapping("/courses/{courseId}/users/{userId}")
    public ResponseEntity<AbandonmentStatusDTO> getUserAbandonmentStatus(
            @PathVariable Long courseId,
            @PathVariable Long userId) {
        AbandonmentStatus status = abandonmentService.getStatus(userId, courseId);
        return ResponseEntity.ok(toDTO(userId, courseId, status));
    }

    /**
     * GET /api/abandonment/courses/{courseId}
     * Returns the abandonment status for every enrolled user in a course.
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<List<AbandonmentStatusDTO>> getCourseAbandonmentStatuses(
            @PathVariable Long courseId) {
        List<CourseMetricSnapshot> snapshots =
                snapshotRepository.findByCourseId(courseId, PageRequest.of(0, 1000)).getContent();

        List<AbandonmentStatusDTO> result = snapshots.stream()
                .map(s -> {
                    AbandonmentStatus status = abandonmentService.getStatus(s.getUserId(), courseId);
                    return toDTO(s.getUserId(), courseId, status);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/abandonment/courses/{courseId}/abandoned
     * Returns only users that are currently abandoned (active COURSE_ABANDONED cycle).
     */
    @GetMapping("/courses/{courseId}/abandoned")
    public ResponseEntity<List<AbandonmentStatusDTO>> getAbandonedUsers(
            @PathVariable Long courseId) {
        List<CourseMetricSnapshot> snapshots =
                snapshotRepository.findByCourseId(courseId, PageRequest.of(0, 1000)).getContent();

        List<AbandonmentStatusDTO> result = snapshots.stream()
                .map(s -> {
                    AbandonmentStatus status = abandonmentService.getStatus(s.getUserId(), courseId);
                    return toDTO(s.getUserId(), courseId, status);
                })
                .filter(dto -> Boolean.TRUE.equals(dto.isAbandoned()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/abandonment/courses/{courseId}/returned
     * Returns only users that have returned after a prior abandonment.
     */
    @GetMapping("/courses/{courseId}/returned")
    public ResponseEntity<List<AbandonmentStatusDTO>> getReturnedUsers(
            @PathVariable Long courseId) {
        List<CourseMetricSnapshot> snapshots =
                snapshotRepository.findByCourseId(courseId, PageRequest.of(0, 1000)).getContent();

        List<AbandonmentStatusDTO> result = snapshots.stream()
                .map(s -> {
                    AbandonmentStatus status = abandonmentService.getStatus(s.getUserId(), courseId);
                    return toDTO(s.getUserId(), courseId, status);
                })
                .filter(dto -> dto.isReturnedAfterAbandonment())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // -----------------------------------------------------------------------

    private AbandonmentStatusDTO toDTO(Long userId, Long courseId, AbandonmentStatus status) {
        AbandonmentStatusDTO dto = new AbandonmentStatusDTO();
        dto.setUserId(userId);
        dto.setCourseId(courseId);
        dto.setAbandoned(status.isAbandoned());
        dto.setAbandonedAt(status.getAbandonedAt());
        dto.setReturnedAfterAbandonment(status.isReturnedAfterAbandonment());
        dto.setReturnedAt(status.getReturnedAt());
        dto.setAbandonmentStatus(status.getAbandonmentStatus());
        return dto;
    }
}
