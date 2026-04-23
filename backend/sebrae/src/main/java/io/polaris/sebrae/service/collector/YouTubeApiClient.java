package io.polaris.sebrae.service.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.polaris.sebrae.config.YouTubeProperties;
import io.polaris.sebrae.dto.YouTubeCommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class YouTubeApiClient {
    private static final Logger logger = LoggerFactory.getLogger(YouTubeApiClient.class);
    
    private final YouTubeProperties youTubeProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public YouTubeApiClient(YouTubeProperties youTubeProperties, ObjectMapper objectMapper) {
        this.youTubeProperties = youTubeProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl("https://www.googleapis.com/youtube/v3").build();
    }

    public List<YouTubeCommentDTO> fetchCommentsByChannel(String channelId) {
        List<YouTubeCommentDTO> comments = new ArrayList<>();
        try {
            String apiKey = youTubeProperties.getKey();
            int maxResults = youTubeProperties.getMaxResults();
            if (apiKey == null || apiKey.isEmpty() || "changeme".equals(apiKey)) {
                logger.warn("YouTube API key is not valid, skipping comments fetch.");
                return comments;
            }

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/commentThreads")
                            .queryParam("part", "snippet")
                            .queryParam("allThreadsRelatedToChannelId", channelId)
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isEmpty()) return comments;

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode items = rootNode.path("items");
            
            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                JsonNode topLevelCommentSnippet = snippet.path("topLevelComment").path("snippet");

                YouTubeCommentDTO dto = new YouTubeCommentDTO();
                dto.setCommentId(item.path("id").asText());
                dto.setVideoId(snippet.path("videoId").asText());
                dto.setAuthorName(topLevelCommentSnippet.path("authorDisplayName").asText());
                dto.setText(topLevelCommentSnippet.path("textDisplay").asText());
                dto.setPublishedAt(topLevelCommentSnippet.path("publishedAt").asText());
                dto.setVideoUrl("https://youtube.com/watch?v=" + dto.getVideoId());
                
                comments.add(dto);
            }
        } catch (Exception e) {
            logger.error("Error fetching comments from YouTube API", e);
        }
        return comments;
    }
}
