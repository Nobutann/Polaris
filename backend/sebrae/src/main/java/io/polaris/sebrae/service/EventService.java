package io.polaris.sebrae.service;

import org.springframework.stereotype.Service;

import io.polaris.sebrae.dto.EventRequestDTO;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.repository.EventRepository;

@Service
public class EventService {
	
	private final EventRepository eventRepository;
	
	public EventService(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}
	
	public Event save(EventRequestDTO dto) {
		Event event = new Event(dto.getUserId(), dto.getCourseId(), dto.getLessonId(), dto.getType(), dto.getMetadata(), dto.getDevice(), dto.getBrowser());
		
		return eventRepository.save(event);
	}
}