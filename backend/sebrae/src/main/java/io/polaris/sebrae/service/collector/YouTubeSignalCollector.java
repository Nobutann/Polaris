package io.polaris.sebrae.service.collector;

import io.polaris.sebrae.config.YouTubeProperties;
import io.polaris.sebrae.dto.YouTubeCommentDTO;
import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.model.enums.SignalSource;
import io.polaris.sebrae.repository.SignalRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class YouTubeSignalCollector implements ExternalSignalCollector {

    private final YouTubeApiClient youTubeApiClient;
    private final YouTubeProperties youTubeProperties;
    private final SignalRepository signalRepository;

    public YouTubeSignalCollector(YouTubeApiClient youTubeApiClient, YouTubeProperties youTubeProperties, SignalRepository signalRepository) {
        this.youTubeApiClient = youTubeApiClient;
        this.youTubeProperties = youTubeProperties;
        this.signalRepository = signalRepository;
    }

    @Override
    public List<Signal> collect() {
        List<Signal> signals = new ArrayList<>();
        String channelId = youTubeProperties.getChannelId();
        if (channelId == null || channelId.isEmpty()) {
            return signals;
        }

        List<YouTubeCommentDTO> comments;
        try {
            comments = youTubeApiClient.fetchCommentsByChannel(channelId);
        } catch (Exception e) {
            return signals;
        }
        
        for (YouTubeCommentDTO comment : comments) {
            String externalId = comment.getCommentId();
            if (externalId != null && signalRepository.existsBySourceAndExternalId(SignalSource.YOUTUBE, externalId)) {
                continue;
            }

            Signal signal = new Signal();
            signal.setSource(SignalSource.YOUTUBE);
            signal.setType("YOUTUBE_COMMENT");
            signal.setExternalId(externalId);
            signal.setExternalUrl(comment.getVideoUrl());
            signal.setContent(comment.getText());
            
            // Basic JSON mapping for metadata
            String author = comment.getAuthorName() != null ? comment.getAuthorName().replace("\"", "\\\"") : "";
            String publishedAt = comment.getPublishedAt() != null ? comment.getPublishedAt().replace("\"", "\\\"") : "";
            String metadata = String.format("{\"author\":\"%s\",\"publishedAt\":\"%s\"}", author, publishedAt);
            signal.setMetadata(metadata);
            
            signals.add(signal);
        }
        return signals;
    }
}
