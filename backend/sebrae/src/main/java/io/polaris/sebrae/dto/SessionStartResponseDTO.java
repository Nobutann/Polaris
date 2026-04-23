package io.polaris.sebrae.dto;

import java.time.LocalDateTime;

public class SessionStartResponseDTO {
    
    private Long sessionId;
    private LocalDateTime startedAt;

    public SessionStartResponseDTO() {}

    public SessionStartResponseDTO(Long sessionId, LocalDateTime startedAt) {
        this.sessionId = sessionId;
        this.startedAt = startedAt;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}
