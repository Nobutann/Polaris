package io.polaris.sebrae.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.polaris.sebrae.dto.EventRequestDTO;
import io.polaris.sebrae.model.Event;
import io.polaris.sebrae.service.EventService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {
	
	private final EventService eventService;
	
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}
	
	@PostMapping
	public ResponseEntity<Event> receive(@Valid @RequestBody EventRequestDTO dto) {
		Event saved = eventService.save(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}
}
