ALTER TABLE course_metric_snapshots
    ADD COLUMN completion_ratio          NUMERIC(5, 2),
    ADD COLUMN retained_7d               BOOLEAN,
    ADD COLUMN retained_14d              BOOLEAN,
    ADD COLUMN retained_30d              BOOLEAN,
    ADD COLUMN first_relevant_activity_at TIMESTAMP,
    ADD COLUMN advance_depth             NUMERIC(5, 2);
