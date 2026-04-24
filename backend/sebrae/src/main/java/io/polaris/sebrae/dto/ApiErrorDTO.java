package io.polaris.sebrae.dto;

import java.util.Map;

public class ApiErrorDTO {

    private String error;
    private String timestamp;
    private Map<String, String> fields;

    public ApiErrorDTO() {}

    public ApiErrorDTO(String error, String timestamp) {
        this.error = error;
        this.timestamp = timestamp;
    }

    public ApiErrorDTO(String error, String timestamp, Map<String, String> fields) {
        this.error = error;
        this.timestamp = timestamp;
        this.fields = fields;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
