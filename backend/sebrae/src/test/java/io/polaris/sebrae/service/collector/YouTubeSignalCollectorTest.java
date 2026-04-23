package io.polaris.sebrae.service.collector;

import io.polaris.sebrae.config.YouTubeProperties;
import io.polaris.sebrae.dto.YouTubeCommentDTO;
import io.polaris.sebrae.model.Signal;
import io.polaris.sebrae.repository.SignalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;

class YouTubeSignalCollectorTest {

    @Mock
    private YouTubeApiClient youTubeApiClient;

    @Mock
    private YouTubeProperties youTubeProperties;

    @Mock
    private SignalRepository signalRepository;

    @InjectMocks
    private YouTubeSignalCollector youTubeSignalCollector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCollectOnlyNewComments() {
        when(youTubeProperties.getChannelId()).thenReturn("channel123");

        YouTubeCommentDTO newComment = new YouTubeCommentDTO();
        newComment.setCommentId("new-id");
        newComment.setText("New comment");

        YouTubeCommentDTO oldComment = new YouTubeCommentDTO();
        oldComment.setCommentId("old-id");
        oldComment.setText("Old comment");

        when(youTubeApiClient.fetchCommentsByChannel("channel123"))
                .thenReturn(List.of(newComment, oldComment));

        when(signalRepository.existsBySourceAndExternalId(io.polaris.sebrae.model.enums.SignalSource.YOUTUBE, "new-id")).thenReturn(false);
        when(signalRepository.existsBySourceAndExternalId(io.polaris.sebrae.model.enums.SignalSource.YOUTUBE, "old-id")).thenReturn(true);

        List<Signal> signals = youTubeSignalCollector.collect();

        assertEquals(1, signals.size());
        assertEquals("new-id", signals.get(0).getExternalId());
    }

    @Test
    void shouldReturnEmptyIfApiThrowsException() {
        when(youTubeProperties.getChannelId()).thenReturn("channel123");
        when(youTubeApiClient.fetchCommentsByChannel("channel123")).thenThrow(new RuntimeException("API error"));

        List<Signal> signals = youTubeSignalCollector.collect();

        assertEquals(0, signals.size());
    }

    @Test
    void shouldReturnEmptyIfNoChannelId() {
        when(youTubeProperties.getChannelId()).thenReturn(null);

        List<Signal> signals = youTubeSignalCollector.collect();

        assertEquals(0, signals.size());
    }
}
