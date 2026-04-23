package io.polaris.sebrae.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import io.polaris.sebrae.dto.EventRequestDTO;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.repository.EventRepository;
import io.polaris.sebrae.dto.InactivityRequestDTO;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import org.springframework.beans.factory.annotation.Value;

@Service
public class EventService {
	
	private static final Logger logger = LoggerFactory.getLogger(EventService.class);

	private final EventRepository eventRepository;
	private final SignalService signalService;
	private final CourseLessonCountService courseLessonCountService;
	private final AuditLogger auditLogger;
	private final long validUserIdMax;
	
	public EventService(EventRepository eventRepository, SignalService signalService, CourseLessonCountService courseLessonCountService, AuditLogger auditLogger, @Value("${polaris.valid-user-id-max:0}") long validUserIdMax) {
		this.eventRepository = eventRepository;
		this.signalService = signalService;
		this.courseLessonCountService = courseLessonCountService;
		this.auditLogger = auditLogger;
		this.validUserIdMax = validUserIdMax;
	}
	
	@Transactional
	public Event save(EventRequestDTO dto) {
		if (validUserIdMax > 0 && dto.getUserId() != null && dto.getUserId() > validUserIdMax) {
			auditLogger.logInvalidIdReference(null, "/api/events", "userId", dto.getUserId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "userId inválido");
		}

		Integer totalLessons = courseLessonCountService.getTotalLessons(dto.getCourseId());
		if (totalLessons == null) {
			auditLogger.logInvalidIdReference(null, "/api/events", "courseId", dto.getCourseId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "courseId inválido");
		}

		if (dto.getLessonId() != null && dto.getLessonId() > totalLessons) {
			auditLogger.logInvalidIdReference(null, "/api/events", "lessonId", dto.getLessonId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "lessonId fora dos limites");
		}

		if ((dto.getType() == EventType.LESSON_STARTED ||
			 dto.getType() == EventType.LESSON_COMPLETED ||
			 dto.getType() == EventType.LESSON_REVISITED ||
			 dto.getType() == EventType.LESSON_RESUMED) && dto.getLessonId() == null) {
			logger.warn("Relevant lesson activity received without lessonId: userId={}, courseId={}, type={}", 
				dto.getUserId(), dto.getCourseId(), dto.getType());
		}

		Event event = new Event(dto.getUserId(), dto.getCourseId(), dto.getLessonId(), dto.getType(), dto.getMetadata(), dto.getDevice(), dto.getBrowser());
		
		event = eventRepository.save(event);
		signalService.save(toSignal(event));
		return event;
	}

	@Transactional
	public void registerInactivity(InactivityRequestDTO dto) {
		if (validUserIdMax > 0 && dto.getUserId() != null && dto.getUserId() > validUserIdMax) {
			auditLogger.logInvalidIdReference(null, "/api/events/inactivity", "userId", dto.getUserId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "userId inválido");
		}

		Integer totalLessons = courseLessonCountService.getTotalLessons(dto.getCourseId());
		if (totalLessons == null) {
			auditLogger.logInvalidIdReference(null, "/api/events/inactivity", "courseId", dto.getCourseId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "courseId inválido");
		}

		if (dto.getLessonId() != null && dto.getLessonId() > totalLessons) {
			auditLogger.logInvalidIdReference(null, "/api/events/inactivity", "lessonId", dto.getLessonId());
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "lessonId fora dos limites");
		}

		Event event = new Event();

		event.setUserId(dto.getUserId());
		event.setCourseId(dto.getCourseId());
		event.setLessonId(dto.getLessonId());
		event.setDevice(dto.getDevice());
		event.setBrowser(dto.getBrowser());
		event.setType(EventType.SCREEN_INACTIVE);

		event = eventRepository.save(event);
		signalService.save(toSignal(event));
	}

	private Signal toSignal(Event event) {
		Signal signal = new Signal();
		signal.setSource(SignalSource.INTERNAL);
		signal.setType(event.getType() != null ? event.getType().name() : null);
		signal.setUserId(event.getUserId());
		signal.setCourseId(event.getCourseId());
		signal.setLessonId(event.getLessonId());
		signal.setContent(null);

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> metaMap = new HashMap<>();
		metaMap.put("device", event.getDevice() != null ? event.getDevice() : "");
		metaMap.put("browser", event.getBrowser() != null ? event.getBrowser() : "");
		
		try {
			if (event.getMetadata() != null && !event.getMetadata().trim().isEmpty()) {
				metaMap.put("eventMetadata", mapper.readTree(event.getMetadata()));
			} else {
				metaMap.put("eventMetadata", mapper.createObjectNode());
			}
			signal.setMetadata(mapper.writeValueAsString(metaMap));
		} catch (Exception e) {
			signal.setMetadata("{}");
		}

		return signal;
	}
}