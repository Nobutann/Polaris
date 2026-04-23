package io.polaris.sebrae.dto;

import io.polaris.sebrae.model.Event;
import java.time.format.DateTimeFormatter;

public class EventResponseDTO {
    private Long id;
    private String type;
    private String timestamp;

    public EventResponseDTO() {}

    public EventResponseDTO(Event event) {
        this.id = event.getId();
        this.type = event.getType() != null ? event.getType().name() : null;
        this.timestamp = event.getTimestamp() != null ? event.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME) : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
