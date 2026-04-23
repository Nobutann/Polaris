CREATE TABLE course_metric_snapshots (
    id                        BIGSERIAL PRIMARY KEY,
    user_id                   BIGINT NOT NULL,
    course_id                 BIGINT NOT NULL,
    days_since_last_activity  INTEGER,
    risk_band                 VARCHAR(20),
    return_frequency_30d      INTEGER,
    continuity_rate           NUMERIC(5, 2),
    last_relevant_activity_at TIMESTAMP,
    calculated_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_snapshot_user_course UNIQUE (user_id, course_id)
);
