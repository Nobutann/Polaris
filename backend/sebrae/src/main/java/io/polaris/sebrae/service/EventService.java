package io.polaris.sebrae.service;

import io.polaris.sebrae.dto.InactivityRequestDTO;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.model.enums.EventType;
import io.polaris.sebrae.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    public void registerInactivity(InactivityRequestDTO dto) {
        Event event = new Event();

        event.setUserId(dto.getUserId());
        event.setCourseId(dto.getCourseId());
        event.setLessonId(dto.getLessonId());

        event.setType(EventType.SCREEN_INACTIVE);

        repository.save(event);
    }
}
