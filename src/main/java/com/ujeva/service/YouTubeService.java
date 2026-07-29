package com.ujeva.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Obtiene los videos subidos más recientes del canal vía YouTube Data API v3.
 *
 * <p>Flujo: {@code channels} (para la playlist de subidas del canal) →
 * {@code playlistItems} (los videos de esa playlist) → se mapean a
 * {@link CachedPost} de tipo {@link PostType#RECENT}. Ante cualquier error de la
 * API se lanza {@link YouTubeApiException}.
 */
@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeService.class);
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    private final RestClient restClient;
    private final String apiKey;
    private final String channelId;

    public YouTubeService(
            RestClient.Builder restClientBuilder,
            @Value("${youtube.api.key:}") String apiKey,
            @Value("${youtube.channel.id}") String channelId) {
        this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
        this.apiKey = apiKey;
        this.channelId = channelId;
    }

    /**
     * Devuelve hasta {@code maxResults} videos recientes del canal como
     * {@code CachedPost(RECENT)}. Lista vacía si el canal no tiene playlist de
     * subidas o sin items.
     *
     * @throws YouTubeApiException si la API responde con error o no se puede leer
     */
    public List<CachedPost> fetchRecentUploads(int maxResults) {
        try {
            String uploadsPlaylistId = fetchUploadsPlaylistId();
            if (uploadsPlaylistId == null) {
                log.warn("El canal {} no expone una playlist de subidas.", channelId);
                return List.of();
            }

            PlaylistItemsResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/playlistItems")
                            .queryParam("part", "snippet")
                            .queryParam("playlistId", uploadsPlaylistId)
                            .queryParam("maxResults", maxResults)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(PlaylistItemsResponse.class);

            if (response == null || response.items() == null) {
                return List.of();
            }
            return response.items().stream()
                    .map(this::toCachedPost)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (RestClientException ex) {
            throw new YouTubeApiException("Error consultando la YouTube Data API", ex);
        }
    }

    private String fetchUploadsPlaylistId() {
        ChannelListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/channels")
                        .queryParam("part", "contentDetails")
                        .queryParam("id", channelId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .body(ChannelListResponse.class);

        if (response == null || response.items() == null || response.items().isEmpty()) {
            return null;
        }
        ContentDetails details = response.items().get(0).contentDetails();
        if (details == null || details.relatedPlaylists() == null) {
            return null;
        }
        return details.relatedPlaylists().uploads();
    }

    private CachedPost toCachedPost(PlaylistItem item) {
        Snippet snippet = item.snippet();
        if (snippet == null || snippet.resourceId() == null
                || snippet.resourceId().videoId() == null) {
            return null;
        }
        String videoId = snippet.resourceId().videoId();

        CachedPost post = new CachedPost(videoId, PostType.RECENT);
        post.setPlatform("YouTube");
        post.setTitle(snippet.title());
        post.setThumbnailUrl(bestThumbnailUrl(snippet, videoId));
        if (snippet.publishedAt() != null) {
            post.setPublishedAt(Instant.parse(snippet.publishedAt()));
        }
        post.setFetchedAt(Instant.now());
        return post;
    }

    /** Miniatura de la API si viene; si no, se deriva del id (sin cuota extra). */
    private String bestThumbnailUrl(Snippet snippet, String videoId) {
        if (snippet.thumbnails() != null && snippet.thumbnails().high() != null
                && snippet.thumbnails().high().url() != null) {
            return snippet.thumbnails().high().url();
        }
        return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
    }

    // --- DTOs mínimos de la respuesta de la API (se ignoran campos desconocidos). ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelListResponse(List<ChannelItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChannelItem(ContentDetails contentDetails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ContentDetails(RelatedPlaylists relatedPlaylists) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record RelatedPlaylists(String uploads) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistItemsResponse(List<PlaylistItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PlaylistItem(Snippet snippet) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Snippet(
            String title,
            String publishedAt,
            ResourceId resourceId,
            Thumbnails thumbnails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ResourceId(String videoId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnails(Thumbnail high) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Thumbnail(String url) {
    }
}
