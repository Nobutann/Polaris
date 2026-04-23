package io.polaris.sebrae.service;

import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Domain service responsible for the formal abandonment lifecycle.
 *
 * <p>Architectural distinction:
 * <ul>
 *   <li><b>Analytical inference</b> (CourseMetricService): computes risk bands such as
 *       {@code abandono_provavel} from inactivity duration.  These are probabilistic signals.</li>
 *   <li><b>Domain event</b> (this service): persists {@link EventType#COURSE_ABANDONED} /
 *       {@link EventType#COURSE_RETURNED} as immutable facts with timestamps.  A user is
 *       <em>officially</em> abandoned once the 15-day threshold is breached.</li>
 * </ul>
 *
 * <p>Idempotency rules:
 * <ul>
 *   <li>COURSE_ABANDONED is persisted at most once per abandonment cycle.  An "active"
 *       abandonment cycle is defined as the most recent lifecycle event being COURSE_ABANDONED.</li>
 *   <li>COURSE_RETURNED cannot be persisted unless there is a preceding active abandonment.</li>
 * </ul>
 */
@Service
public class AbandonmentService {

    private static final Logger logger = LoggerFactory.getLogger(AbandonmentService.class);

    /** Official abandonment threshold per the story: 15 days without relevant activity. */
    static final int ABANDONMENT_THRESHOLD_DAYS = 15;

    /** Both domain lifecycle event types, used for idempotency queries. */
    private static final Set<EventType> LIFECYCLE_TYPES =
            Set.of(EventType.COURSE_ABANDONED, EventType.COURSE_RETURNED);

    private final EventRepository eventRepository;

    public AbandonmentService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Evaluates the abandonment/return state for a single user/course pair and persists
     * domain events as required.  Safe to call repeatedly (idempotent).
     *
     * @param userId   the user
     * @param courseId the course
     */
    @Transactional
    public void evaluate(Long userId, Long courseId) {
        Optional<LocalDateTime> lastRelevantOpt =
                eventRepository.findLastRelevantActivityAt(userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES);

        // Find the most recent lifecycle event to determine the current cycle state
        List<Event> latestLifecycle = eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                userId, courseId, LIFECYCLE_TYPES, PageRequest.of(0, 1));

        Event lastLifecycle = latestLifecycle.isEmpty() ? null : latestLifecycle.get(0);

        boolean isCurrentlyAbandoned = lastLifecycle != null
                && lastLifecycle.getType() == EventType.COURSE_ABANDONED;

        if (isCurrentlyAbandoned) {
            // --- Check for RETURN ---
            // A return is recognized when there is relevant activity AFTER the abandonment timestamp.
            LocalDateTime abandonedAt = lastLifecycle.getTimestamp();
            Optional<LocalDateTime> activityAfterAbandonment =
                    eventRepository.findLastRelevantActivityAfter(
                            userId, courseId, EventType.RELEVANT_ACTIVITY_TYPES, abandonedAt);

            if (activityAfterAbandonment.isPresent()) {
                logger.info("User {} returned to course {} after abandonment at {}.",
                        userId, courseId, abandonedAt);
                Event returned = new Event();
                returned.setUserId(userId);
                returned.setCourseId(courseId);
                returned.setType(EventType.COURSE_RETURNED);
                returned.setMetadata("{\"previousAbandonmentAt\":\"" + abandonedAt + "\"}");
                eventRepository.save(returned);
            }
            // else: user is still abandoned and inactive — do nothing (no duplicate)

        } else {
            // --- Check for ABANDONMENT ---
            if (lastRelevantOpt.isEmpty()) {
                // No relevant activity at all — cannot determine abandonment without context.
                // The scheduler only calls evaluate() for pairs that have at least one relevant event;
                // this branch is a safety guard.
                return;
            }

            LocalDateTime lastRelevant = lastRelevantOpt.get();
            long daysSince = ChronoUnit.DAYS.between(lastRelevant.toLocalDate(), LocalDate.now());

            if (daysSince >= ABANDONMENT_THRESHOLD_DAYS) {
                logger.info("User {} abandoned course {} after {} days of inactivity. Last activity: {}.",
                        userId, courseId, daysSince, lastRelevant);
                Event abandoned = new Event();
                abandoned.setUserId(userId);
                abandoned.setCourseId(courseId);
                abandoned.setType(EventType.COURSE_ABANDONED);
                abandoned.setMetadata("{\"daysSinceLastActivity\":" + daysSince + "}");
                eventRepository.save(abandoned);
            }
            // else: user is active (or returned and now in a non-abandoned state) — nothing to do
        }
    }

    /**
     * DTO-like projection of the current abandonment state for a user/course pair.
     * Derived purely from persisted domain events.
     */
    public AbandonmentStatus getStatus(Long userId, Long courseId) {
        List<Event> latestLifecycle = eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                userId, courseId, LIFECYCLE_TYPES, PageRequest.of(0, 1));

        if (latestLifecycle.isEmpty()) {
            return AbandonmentStatus.notAbandoned();
        }

        Event last = latestLifecycle.get(0);

        if (last.getType() == EventType.COURSE_ABANDONED) {
            return AbandonmentStatus.abandoned(last.getTimestamp());
        } else {
            // COURSE_RETURNED — find the preceding abandonment for the timestamp
            List<Event> allLifecycle = eventRepository.findLatestByUserIdAndCourseIdAndTypes(
                    userId, courseId, LIFECYCLE_TYPES, PageRequest.of(0, 20));

            LocalDateTime abandonedAt = null;
            for (Event e : allLifecycle) {
                if (e.getType() == EventType.COURSE_ABANDONED) {
                    abandonedAt = e.getTimestamp();
                    break;
                }
            }
            return AbandonmentStatus.returned(abandonedAt, last.getTimestamp());
        }
    }

    // -----------------------------------------------------------------------
    // Value object representing the current abandonment state
    // -----------------------------------------------------------------------

    public static class AbandonmentStatus {
        private final boolean abandoned;
        private final LocalDateTime abandonedAt;
        private final boolean returnedAfterAbandonment;
        private final LocalDateTime returnedAt;

        private AbandonmentStatus(boolean abandoned, LocalDateTime abandonedAt,
                                  boolean returnedAfterAbandonment, LocalDateTime returnedAt) {
            this.abandoned = abandoned;
            this.abandonedAt = abandonedAt;
            this.returnedAfterAbandonment = returnedAfterAbandonment;
            this.returnedAt = returnedAt;
        }

        static AbandonmentStatus notAbandoned() {
            return new AbandonmentStatus(false, null, false, null);
        }

        static AbandonmentStatus abandoned(LocalDateTime abandonedAt) {
            return new AbandonmentStatus(true, abandonedAt, false, null);
        }

        static AbandonmentStatus returned(LocalDateTime abandonedAt, LocalDateTime returnedAt) {
            return new AbandonmentStatus(false, abandonedAt, true, returnedAt);
        }

        public boolean isAbandoned() { return abandoned; }
        public LocalDateTime getAbandonedAt() { return abandonedAt; }
        public boolean isReturnedAfterAbandonment() { return returnedAfterAbandonment; }
        public LocalDateTime getReturnedAt() { return returnedAt; }

        /** Single-string status useful for UIs and API responses. */
        public String getAbandonmentStatus() {
            if (abandoned) return "ABANDONED";
            if (returnedAfterAbandonment) return "RETURNED";
            return "ACTIVE";
        }
    }
}
