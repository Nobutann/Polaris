package io.polaris.sebrae.model.enums;

import java.util.Set;

public enum EventType {
	// Relevantes (indicam engajamento real)
	LESSON_STARTED,
	LESSON_COMPLETED,
	LESSON_REVISITED,
	LESSON_RESUMED,
	PAGE_COMPLETED,
	QUIZ_ANSWERED,
	MATERIAL_DOWNLOADED,

	// Não Relevantes (interações rasas, passivas ou encerramentos)
	VIDEO_PAUSED,
	VIDEO_RESUMED,
	VIDEO_ACCELERATED,
	VIDEO_REWOUND,
	PAGE_OPENED,
	READING_TIME,
	EXTERNAL_LINK_CLICKED,
	CONTENT_COPIED,
	CONTENT_EXPANDED,
	SCREEN_INACTIVE,
	SESSION_ENDED,

	// Eventos formais de domínio — ciclo de vida de abandono
	// Persistidos pelo AbandonmentService; NÃO contam como atividade relevante
	COURSE_ABANDONED,
	COURSE_RETURNED;

	public static final Set<EventType> RELEVANT_ACTIVITY_TYPES = Set.of(
			LESSON_STARTED,
			LESSON_COMPLETED,
			LESSON_REVISITED,
			LESSON_RESUMED,
			PAGE_COMPLETED,
			QUIZ_ANSWERED,
			MATERIAL_DOWNLOADED
	);
}
