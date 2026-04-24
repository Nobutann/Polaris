-- ==============================================================================
-- CARGA FAKE DE DESENVOLVIMENTO
-- ==============================================================================
-- Este arquivo data.sql contem apenas dados ficticios para fins de demonstracao,
-- desenvolvimento local e testes do frontend. 
-- NAO DEVE SER UTILIZADO EM PRODUCAO!
-- ==============================================================================

-- 0. Limpeza prévia para evitar duplicatas em reinicializações do Spring Boot
DELETE FROM events WHERE user_id IN (1001, 1002, 1003, 1004);
DELETE FROM sessions WHERE user_id IN (1001, 1002, 1003, 1004);
DELETE FROM signals WHERE user_id IN (1001, 1002, 1003, 1004);
DELETE FROM course_metric_snapshots WHERE user_id IN (1001, 1002, 1003, 1004);
DELETE FROM course_lesson_counts WHERE course_id IN (101, 102, 103);

-- 1. Course Lesson Counts (Total de aulas de cursos fictícios)
INSERT INTO course_lesson_counts (course_id, total_lessons) VALUES
(101, 20),
(102, 15),
(103, 10)
ON CONFLICT (course_id) DO NOTHING;

-- 2. Sessions (Sessões fictícias de usuários)
INSERT INTO sessions (user_id, course_id, start_time, end_time, device, browser) VALUES
(1001, 101, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days' + INTERVAL '45 minutes', 'Desktop', 'Chrome'),
(1001, 101, NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days' + INTERVAL '60 minutes', 'Desktop', 'Chrome'),
(1001, 101, NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days' + INTERVAL '30 minutes', 'Mobile', 'Safari'),
(1002, 102, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days' + INTERVAL '120 minutes', 'Desktop', 'Firefox'),
(1003, 101, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '20 minutes', 'Mobile', 'Chrome'),
(1004, 103, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days' + INTERVAL '10 minutes', 'Desktop', 'Edge');

-- 3. Events (Eventos de progresso e engajamento)
-- Usuário 1001 (Progresso consistente no Curso 101, risco baixo)
INSERT INTO events (user_id, course_id, lesson_id, type, metadata, device, browser, timestamp) VALUES
(1001, 101, 1, 'LESSON_STARTED', '{"source": "dashboard"}', 'Desktop', 'Chrome', NOW() - INTERVAL '10 days'),
(1001, 101, 1, 'LESSON_COMPLETED', '{"score": 10}', 'Desktop', 'Chrome', NOW() - INTERVAL '10 days' + INTERVAL '20 minutes'),
(1001, 101, 2, 'LESSON_STARTED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '10 days' + INTERVAL '21 minutes'),
(1001, 101, 2, 'LESSON_COMPLETED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '10 days' + INTERVAL '40 minutes'),
(1001, 101, 3, 'LESSON_STARTED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '5 days'),
(1001, 101, 3, 'LESSON_COMPLETED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '5 days' + INTERVAL '25 minutes'),
(1001, 101, 4, 'LESSON_STARTED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '5 days' + INTERVAL '26 minutes'),
(1001, 101, 4, 'LESSON_COMPLETED', '{}', 'Desktop', 'Chrome', NOW() - INTERVAL '5 days' + INTERVAL '50 minutes'),
(1001, 101, 5, 'LESSON_STARTED', '{}', 'Mobile', 'Safari', NOW() - INTERVAL '1 days'),
(1001, 101, 5, 'LESSON_COMPLETED', '{}', 'Mobile', 'Safari', NOW() - INTERVAL '1 days' + INTERVAL '25 minutes');

-- Usuário 1002 (Curso 102, risco médio/atenção)
INSERT INTO events (user_id, course_id, lesson_id, type, metadata, device, browser, timestamp) VALUES
(1002, 102, 1, 'LESSON_STARTED', '{}', 'Desktop', 'Firefox', NOW() - INTERVAL '15 days'),
(1002, 102, 1, 'LESSON_COMPLETED', '{}', 'Desktop', 'Firefox', NOW() - INTERVAL '15 days' + INTERVAL '30 minutes'),
(1002, 102, 2, 'LESSON_STARTED', '{}', 'Desktop', 'Firefox', NOW() - INTERVAL '15 days' + INTERVAL '35 minutes'),
(1002, 102, 2, 'VIDEO_PAUSED', '{"time": 120}', 'Desktop', 'Firefox', NOW() - INTERVAL '15 days' + INTERVAL '50 minutes');

-- Usuário 1003 (Curso 101, recém começou, normal)
INSERT INTO events (user_id, course_id, lesson_id, type, metadata, device, browser, timestamp) VALUES
(1003, 101, 1, 'LESSON_STARTED', '{}', 'Mobile', 'Chrome', NOW() - INTERVAL '2 days'),
(1003, 101, 1, 'QUIZ_ANSWERED', '{"score": 8}', 'Mobile', 'Chrome', NOW() - INTERVAL '2 days' + INTERVAL '10 minutes');

-- Usuário 1004 (Curso 103, abandono provável)
INSERT INTO events (user_id, course_id, lesson_id, type, metadata, device, browser, timestamp) VALUES
(1004, 103, 1, 'LESSON_STARTED', '{}', 'Desktop', 'Edge', NOW() - INTERVAL '20 days'),
(1004, 103, 1, 'COURSE_ABANDONED', '{}', 'System', 'System', NOW() - INTERVAL '5 days');

-- 4. Signals (Sinais coletados do frontend)
INSERT INTO signals (source, type, user_id, course_id, lesson_id, external_id, external_url, content, metadata, collected_at) VALUES
('frontend', 'VIDEO_PLAY', 1001, 101, 1, 'vid-101-1', 'http://polarisfake.com/course/101/lesson/1', 'Video started', '{"duration": 1200}', NOW() - INTERVAL '10 days'),
('frontend', 'VIDEO_PAUSE', 1002, 102, 2, 'vid-102-2', 'http://polarisfake.com/course/102/lesson/2', 'Video paused', '{"time": 120}', NOW() - INTERVAL '15 days' + INTERVAL '50 minutes'),
('frontend', 'PAGE_SCROLL', 1003, 101, 1, 'page-101-1', 'http://polarisfake.com/course/101/lesson/1', 'Scrolled to bottom', '{"depth": 100}', NOW() - INTERVAL '2 days' + INTERVAL '5 minutes');

-- 5. Course Metric Snapshots (Métricas pré-calculadas para os dashboards)
INSERT INTO course_metric_snapshots (
    user_id, course_id, days_since_last_activity, risk_band, return_frequency_30d, 
    continuity_rate, last_relevant_activity_at, calculated_at, completion_ratio, 
    retained_7d, retained_14d, retained_30d, first_relevant_activity_at, advance_depth
) VALUES
-- Usuário 1001 (Progresso Consistente, Risco Normal)
(1001, 101, 1, 'normal', 3, 1.00, NOW() - INTERVAL '1 days', NOW(), 1.00, true, true, true, NOW() - INTERVAL '10 days', 0.25),

-- Usuário 1002 (Sem atividade recente, Risco)
(1002, 102, 15, 'risco', 1, 0.00, NOW() - INTERVAL '15 days' + INTERVAL '30 minutes', NOW(), 0.50, false, false, true, NOW() - INTERVAL '15 days', 0.06),

-- Usuário 1003 (Recente, Normal)
(1003, 101, 2, 'normal', 1, 0.00, NOW() - INTERVAL '2 days', NOW(), 0.00, false, false, true, NOW() - INTERVAL '2 days', 0.05),

-- Usuário 1004 (Abandono Provável)
(1004, 103, 20, 'abandono_provavel', 1, 0.00, NOW() - INTERVAL '20 days', NOW(), 0.00, false, false, false, NOW() - INTERVAL '20 days', 0.10)
ON CONFLICT (user_id, course_id) DO UPDATE SET
    days_since_last_activity = EXCLUDED.days_since_last_activity,
    risk_band = EXCLUDED.risk_band,
    return_frequency_30d = EXCLUDED.return_frequency_30d,
    continuity_rate = EXCLUDED.continuity_rate,
    last_relevant_activity_at = EXCLUDED.last_relevant_activity_at,
    calculated_at = EXCLUDED.calculated_at,
    completion_ratio = EXCLUDED.completion_ratio,
    retained_7d = EXCLUDED.retained_7d,
    retained_14d = EXCLUDED.retained_14d,
    retained_30d = EXCLUDED.retained_30d,
    first_relevant_activity_at = EXCLUDED.first_relevant_activity_at,
    advance_depth = EXCLUDED.advance_depth;
