package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.InactivityRequestDTO;
import io.polaris.sebrae.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping("/inactivity")
    public ResponseEntity<Void> registerInactivity(
        @RequestBody InactivityRequestDTO request
    ) {
        service.registerInactivity(request);

        return ResponseEntity.ok().build();
    }
}
