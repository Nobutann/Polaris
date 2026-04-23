CREATE TABLE course_lesson_counts (
    id           BIGSERIAL PRIMARY KEY,
    course_id    BIGINT NOT NULL,
    total_lessons INTEGER NOT NULL,
    CONSTRAINT uq_course_lesson_count_course UNIQUE (course_id)
);
