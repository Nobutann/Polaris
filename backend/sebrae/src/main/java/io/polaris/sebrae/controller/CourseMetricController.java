package io.polaris.sebrae.controller;

import io.polaris.sebrae.config.BurstDetector;
import io.polaris.sebrae.dto.CourseEvasionPointDTO;
import io.polaris.sebrae.dto.CourseAggregateSummaryDTO;
import io.polaris.sebrae.service.AuditLogger;
import io.polaris.sebrae.service.CourseMetricQueryService;
import io.polaris.sebrae.service.CourseMetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.List;

@RestController
@RequestMapping("/api/metrics/courses")
public class CourseMetricController {

    private final CourseMetricQueryService queryService;
    private final CourseMetricService metricService;
    private final BurstDetector burstDetector;
    private final AuditLogger auditLogger;

    public CourseMetricController(CourseMetricQueryService queryService, CourseMetricService metricService,
                                   BurstDetector burstDetector, AuditLogger auditLogger) {
        this.queryService = queryService;
        this.metricService = metricService;
        this.burstDetector = burstDetector;
        this.auditLogger = auditLogger;
    }

    @GetMapping
    public ResponseEntity<Page<CourseAggregateSummaryDTO>> getAllGlobalSnapshots(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ResponseEntity.ok(queryService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<Page<CourseAggregateSummaryDTO>> getAllSnapshotsForCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return ResponseEntity.ok(queryService.getByCourse(courseId, PageRequest.of(page, size)));
    }

    @GetMapping("/{courseId}/evasion-points")
    public ResponseEntity<List<CourseEvasionPointDTO>> getEvasionPoints(@PathVariable Long courseId) {
        return ResponseEntity.ok(queryService.getEvasionPoints(courseId));
    }

    @PostMapping("/{courseId}/recalculate")
    public ResponseEntity<Void> recalculateCourse(@PathVariable Long courseId, HttpServletRequest request) {
        String ip = auditLogger.getClientIP();
        String source = request.getHeader("X-Internal-Source");
        burstDetector.record(ip, source, "/api/metrics/courses/" + courseId + "/recalculate");
        metricService.recalculateCourse(courseId);
        return ResponseEntity.ok().build();
    }
}
