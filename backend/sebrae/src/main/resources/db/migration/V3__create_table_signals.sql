CREATE TABLE signals (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    type VARCHAR(100) NOT NULL,
    user_id BIGINT,
    course_id BIGINT,
    lesson_id BIGINT,
    external_id VARCHAR(255),
    external_url TEXT,
    content TEXT,
    metadata TEXT,
    collected_at TIMESTAMP NOT NULL DEFAULT NOW()
);
