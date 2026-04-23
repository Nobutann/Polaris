package io.polaris.sebrae.service;

import io.polaris.sebrae.dto.SessionRequestDTO;
import io.polaris.sebrae.model.Session;
import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import io.polaris.sebrae.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SignalService signalService;
    private final AuditLogger auditLogger;
    private final CourseLessonCountService courseLessonCountService;
    private final ObjectMapper objectMapper;
    private final long validUserIdMax;

    public SessionService(SessionRepository sessionRepository, SignalService signalService, AuditLogger auditLogger, CourseLessonCountService courseLessonCountService, @Value("${polaris.valid-user-id-max:0}") long validUserIdMax) {
        this.sessionRepository = sessionRepository;
        this.signalService = signalService;
        this.auditLogger = auditLogger;
        this.courseLessonCountService = courseLessonCountService;
        this.objectMapper = new ObjectMapper();
        this.validUserIdMax = validUserIdMax;
    }

    @Transactional
    public Session start(SessionRequestDTO dto) {
        if (validUserIdMax > 0 && dto.getUserId() != null && dto.getUserId() > validUserIdMax) {
            auditLogger.logInvalidIdReference(null, "/api/sessions", "userId", dto.getUserId());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "userId inválido");
        }

        if (dto.getCourseId() != null) {
            Integer totalLessons = courseLessonCountService.getTotalLessons(dto.getCourseId());
            if (totalLessons == null) {
                auditLogger.logInvalidIdReference(null, "/api/sessions", "courseId", dto.getCourseId());
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "courseId inválido");
            }
        }

        Session session = new Session(dto.getUserId(), dto.getCourseId(), dto.getDevice(), dto.getBrowser());
        session = sessionRepository.save(session);

        Signal signal = new Signal();
        signal.setSource(SignalSource.INTERNAL);
        signal.setType("SESSION_STARTED");
        signal.setUserId(session.getUserId());
        signal.setCourseId(session.getCourseId());
        
        try {
            Map<String, Object> metaMap = new HashMap<>();
            metaMap.put("sessionId", session.getId());
            metaMap.put("device", session.getDevice() != null ? session.getDevice() : "");
            metaMap.put("browser", session.getBrowser() != null ? session.getBrowser() : "");
            signal.setMetadata(objectMapper.writeValueAsString(metaMap));
        } catch (Exception e) {
            signal.setMetadata("{}");
        }
        
        signalService.save(signal);

        return session;
    }

    @Transactional
    public void end(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> 
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Sessão não encontrada")
        );
        
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);

        long durationSeconds = 0;
        if (session.getStartTime() != null) {
            durationSeconds = ChronoUnit.SECONDS.between(session.getStartTime(), session.getEndTime());
        }

        Signal signal = new Signal();
        signal.setSource(SignalSource.INTERNAL);
        signal.setType("SESSION_ENDED");
        signal.setUserId(session.getUserId());
        signal.setCourseId(session.getCourseId());
        
        try {
            Map<String, Object> metaMap = new HashMap<>();
            metaMap.put("sessionId", session.getId());
            metaMap.put("durationSeconds", durationSeconds);
            signal.setMetadata(objectMapper.writeValueAsString(metaMap));
        } catch (Exception e) {
            signal.setMetadata("{}");
        }

        signalService.save(signal);
    }
}
