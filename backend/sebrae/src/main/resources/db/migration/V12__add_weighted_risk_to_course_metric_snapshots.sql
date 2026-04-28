ALTER TABLE course_metric_snapshots
    ADD COLUMN weighted_risk_score NUMERIC(5, 2),
    ADD COLUMN priority_level      VARCHAR(20),
    ADD COLUMN main_risk_reason    VARCHAR(80);
