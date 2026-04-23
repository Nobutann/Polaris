package io.polaris.sebrae.repository;

import io.polaris.sebrae.model.CourseLessonCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseLessonCountRepository extends JpaRepository<CourseLessonCount, Long> {
    Optional<CourseLessonCount> findByCourseId(Long courseId);
}
