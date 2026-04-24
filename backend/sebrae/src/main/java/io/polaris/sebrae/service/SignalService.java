package io.polaris.sebrae.service;

import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.repository.SignalRepository;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import io.polaris.sebrae.model.enums.SignalSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class SignalService {

    private final SignalRepository signalRepository;
    private final CourseLessonCountService courseLessonCountService;
    private final AuditLogger auditLogger;
    private final long validUserIdMax;

    public SignalService(SignalRepository signalRepository, CourseLessonCountService courseLessonCountService, AuditLogger auditLogger, @Value("${polaris.valid-user-id-max:0}") long validUserIdMax) {
        this.signalRepository = signalRepository;
        this.courseLessonCountService = courseLessonCountService;
        this.auditLogger = auditLogger;
        this.validUserIdMax = validUserIdMax;
    }

    public Signal save(Signal signal) {
        return signalRepository.save(signal);
    }

    public Page<Signal> findAll(Pageable pageable) {
        return signalRepository.findAll(pageable);
    }

    public Page<Signal> findBySource(SignalSource source, Pageable pageable) {
        return signalRepository.findBySource(source, pageable);
    }

    public Signal saveFromRequest(io.polaris.sebrae.dto.SignalRequestDTO dto) {
        if (dto.getSource() == SignalSource.INTERNAL) {
            auditLogger.logInvalidPayload(null, "/api/signals", "source", "EXTERNAL API cannot submit INTERNAL source");
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Source INTERNAL não permitido via API");
        }

        if (validUserIdMax > 0 && dto.getUserId() != null && dto.getUserId() > validUserIdMax) {
            auditLogger.logInvalidIdReference(null, "/api/signals", "userId", dto.getUserId());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "userId inválido");
        }

        if (dto.getCourseId() != null) {
            Integer totalLessons = courseLessonCountService.getTotalLessons(dto.getCourseId());
            if (totalLessons == null) {
                auditLogger.logInvalidIdReference(null, "/api/signals", "courseId", dto.getCourseId());
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "courseId inválido");
            }
            if (dto.getLessonId() != null && dto.getLessonId() > totalLessons) {
                auditLogger.logInvalidIdReference(null, "/api/signals", "lessonId", dto.getLessonId());
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "lessonId fora dos limites");
            }
        } else if (dto.getLessonId() != null) {
            auditLogger.logInvalidIdReference(null, "/api/signals", "lessonId", dto.getLessonId());
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "lessonId requer courseId");
        }

        Signal signal = new Signal();
        signal.setSource(dto.getSource());
        signal.setType(dto.getType());
        signal.setUserId(dto.getUserId());
        signal.setCourseId(dto.getCourseId());
        signal.setLessonId(dto.getLessonId());
        signal.setContent(dto.getContent());
        signal.setMetadata(dto.getMetadata());

        return save(signal);
    }
}
