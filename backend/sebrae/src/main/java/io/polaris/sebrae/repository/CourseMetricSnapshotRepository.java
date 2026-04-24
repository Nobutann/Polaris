package io.polaris.sebrae.repository;

import io.polaris.sebrae.model.CourseMetricSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseMetricSnapshotRepository extends JpaRepository<CourseMetricSnapshot, Long> {
    Optional<CourseMetricSnapshot> findByUserIdAndCourseId(Long userId, Long courseId);
    Page<CourseMetricSnapshot> findByCourseId(Long courseId, Pageable pageable);
    List<CourseMetricSnapshot> findByCourseIdAndRiskBandIn(Long courseId, List<String> bands);
}
