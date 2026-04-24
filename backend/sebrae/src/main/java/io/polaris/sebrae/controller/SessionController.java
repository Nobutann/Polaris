package io.polaris.sebrae.controller;

import io.polaris.sebrae.dto.SessionRequestDTO;
import io.polaris.sebrae.dto.SessionStartResponseDTO;
import io.polaris.sebrae.model.Session;
import io.polaris.sebrae.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionStartResponseDTO> start(@Valid @RequestBody SessionRequestDTO dto) {
        Session session = sessionService.start(dto);
        SessionStartResponseDTO responseDTO = new SessionStartResponseDTO(session.getId(), session.getStartTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<Void> end(@PathVariable Long id) {
        sessionService.end(id);
        return ResponseEntity.ok().build();
    }
}
