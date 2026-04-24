package io.polaris.sebrae.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>{

    // Último evento relevante de um usuário em um curso
    @Query("SELECT MAX(e.timestamp) FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type IN :relevantTypes")
    Optional<LocalDateTime> findLastRelevantActivityAt(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("relevantTypes") Collection<EventType> relevantTypes
    );

    // Eventos relevantes de um usuário em um curso, ordenados por data
    @Query("SELECT e FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type IN :relevantTypes ORDER BY e.timestamp ASC")
    List<Event> findRelevantEventsOrdered(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("relevantTypes") Collection<EventType> relevantTypes
    );

    // Pares distintos (userId, courseId) com ao menos um evento relevante
    @Query("SELECT DISTINCT e.userId, e.courseId FROM Event e WHERE e.type IN :relevantTypes")
    List<Object[]> findDistinctUserCourseWithRelevantActivity(
        @Param("relevantTypes") Collection<EventType> relevantTypes
    );

    @Query("SELECT COUNT(e) FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type = :type")
    long countByUserIdAndCourseIdAndType(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("type") EventType type
    );

    @Query("SELECT MIN(e.timestamp) FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type IN :relevantTypes")
    Optional<LocalDateTime> findFirstRelevantActivityAt(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("relevantTypes") Collection<EventType> relevantTypes
    );

    // -----------------------------------------------------------------------
    // Abandonment domain queries
    // -----------------------------------------------------------------------

    /**
     * Returns the most recent domain lifecycle event (COURSE_ABANDONED / COURSE_RETURNED)
     * for this user/course pair.  Used to determine the current abandonment cycle state.
     */
    @Query("SELECT e FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type IN :types ORDER BY e.timestamp DESC")
    List<Event> findLatestByUserIdAndCourseIdAndTypes(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("types") Collection<EventType> types,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Returns the timestamp of the most recent relevant activity strictly after
     * the given cutoff.  Used to detect a return after abandonment.
     */
    @Query("SELECT MAX(e.timestamp) FROM Event e WHERE e.userId = :userId AND e.courseId = :courseId AND e.type IN :relevantTypes AND e.timestamp > :after")
    Optional<LocalDateTime> findLastRelevantActivityAfter(
        @Param("userId") Long userId,
        @Param("courseId") Long courseId,
        @Param("relevantTypes") Collection<EventType> relevantTypes,
        @Param("after") LocalDateTime after
    );
}
