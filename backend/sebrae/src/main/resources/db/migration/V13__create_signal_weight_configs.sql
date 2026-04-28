-- V13__create_signal_weight_configs.sql
CREATE TABLE signal_weight_configs (
    id          BIGSERIAL PRIMARY KEY,
    signal_key  VARCHAR(40)    NOT NULL UNIQUE,
    label       VARCHAR(80)    NOT NULL,
    weight      NUMERIC(5, 4)  NOT NULL,
    enabled     BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW()
);

INSERT INTO signal_weight_configs (signal_key, label, weight, enabled) VALUES
    ('INACTIVITY',    'Inatividade do aluno',   0.4000, TRUE),
    ('CONTINUITY',    'Taxa de continuidade',   0.2500, TRUE),
    ('COMPLETION',    'Taxa de conclusão',       0.2000, TRUE),
    ('ADVANCE_DEPTH', 'Profundidade de avanço', 0.1500, TRUE);
