package io.polaris.sebrae.dto;

public class YouTubeIngestionResponseDTO {
    private int collected;

    public YouTubeIngestionResponseDTO() {}

    public YouTubeIngestionResponseDTO(int collected) {
        this.collected = collected;
    }

    public int getCollected() {
        return collected;
    }

    public void setCollected(int collected) {
        this.collected = collected;
    }
}
