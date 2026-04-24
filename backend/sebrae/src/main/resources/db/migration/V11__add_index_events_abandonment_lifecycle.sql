-- V11: Add composite index to support efficient abandonment lifecycle queries.
-- The COURSE_ABANDONED / COURSE_RETURNED queries filter by (user_id, course_id, type)
-- and order by timestamp DESC.  This index covers all three predicates.
CREATE INDEX IF NOT EXISTS idx_events_user_course_type_ts
    ON events (user_id, course_id, type, timestamp DESC);
