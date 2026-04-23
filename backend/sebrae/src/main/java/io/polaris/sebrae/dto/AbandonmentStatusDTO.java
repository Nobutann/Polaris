package io.polaris.sebrae.dto;

import java.time.LocalDateTime;

/**
 * API response representing the formal abandonment state of a user/course pair.
 * All fields are derived from persisted domain events (COURSE_ABANDONED / COURSE_RETURNED).
 *
 * <p>This is distinct from {@link CourseAggregateSummaryDTO#getRiskBand()}, which is an
 * analytical inference based on inactivity duration.
 */
public class AbandonmentStatusDTO {

    private Long userId;
    private Long courseId;

    /** True only when the most recent lifecycle event is COURSE_ABANDONED. */
    private boolean abandoned;

    /** Timestamp of the COURSE_ABANDONED event that opened the current cycle. */
    private LocalDateTime abandonedAt;

    /** True when the user has a COURSE_RETURNED event following a prior abandonment. */
    private boolean returnedAfterAbandonment;

    /** Timestamp of the most recent COURSE_RETURNED event. */
    private LocalDateTime returnedAt;

    /**
     * Computed string status: "ABANDONED", "RETURNED", or "ACTIVE".
     * "ACTIVE" means no abandonment event has ever been persisted for this pair.
     */
    private String abandonmentStatus;

    public AbandonmentStatusDTO() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public boolean isAbandoned() { return abandoned; }
    public void setAbandoned(boolean abandoned) { this.abandoned = abandoned; }

    public LocalDateTime getAbandonedAt() { return abandonedAt; }
    public void setAbandonedAt(LocalDateTime abandonedAt) { this.abandonedAt = abandonedAt; }

    public boolean isReturnedAfterAbandonment() { return returnedAfterAbandonment; }
    public void setReturnedAfterAbandonment(boolean returnedAfterAbandonment) {
        this.returnedAfterAbandonment = returnedAfterAbandonment;
    }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public String getAbandonmentStatus() { return abandonmentStatus; }
    public void setAbandonmentStatus(String abandonmentStatus) {
        this.abandonmentStatus = abandonmentStatus;
    }
}
