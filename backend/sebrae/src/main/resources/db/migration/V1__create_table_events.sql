CREATE TABLE events (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL,
	course_id BIGINT NOT NULL,
	lesson_id BIGINT,
	type VARCHAR(50) NOT NULL,
	metadata TEXT,
	device VARCHAR(100),
	browser VARCHAR(100),
	timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);