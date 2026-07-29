package com.ujeva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class YouTubeServiceTest {

    private static final String CHANNELS_JSON = """
            { "items": [ { "contentDetails": {
                "relatedPlaylists": { "uploads": "UU_subidas_123" } } } ] }
            """;

    private static final String ITEMS_JSON = """
            { "items": [
                { "snippet": {
                    "title": "Reflexión de la mañana",
                    "publishedAt": "2026-06-01T10:00:00Z",
                    "resourceId": { "videoId": "vidA" },
                    "thumbnails": { "high": { "url": "https://i.ytimg.com/vi/vidA/hqdefault.jpg" } }
                } },
                { "snippet": {
                    "title": "Testimonio",
                    "publishedAt": "2026-05-01T10:00:00Z",
                    "resourceId": { "videoId": "vidB" },
                    "thumbnails": {}
                } }
            ] }
            """;

    @Test
    void mapeaLaRespuestaDeLaApiACachedPosts() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/channels")))
                .andRespond(withSuccess(CHANNELS_JSON, MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("/playlistItems")))
                .andRespond(withSuccess(ITEMS_JSON, MediaType.APPLICATION_JSON));

        YouTubeService service = new YouTubeService(builder, "api-key", "UC-canal");

        List<CachedPost> posts = service.fetchRecentUploads(6);

        assertThat(posts).hasSize(2);
        assertThat(posts).allSatisfy(p -> {
            assertThat(p.getType()).isEqualTo(PostType.RECENT);
            assertThat(p.getPlatform()).isEqualTo("YouTube");
            assertThat(p.getFetchedAt()).isNotNull();
        });
        assertThat(posts.get(0).getVideoId()).isEqualTo("vidA");
        assertThat(posts.get(0).getTitle()).isEqualTo("Reflexión de la mañana");
        assertThat(posts.get(0).getPublishedAt()).isEqualTo(Instant.parse("2026-06-01T10:00:00Z"));
        assertThat(posts.get(0).getThumbnailUrl()).isEqualTo("https://i.ytimg.com/vi/vidA/hqdefault.jpg");
        // Sin miniatura en la API -> se deriva del id.
        assertThat(posts.get(1).getThumbnailUrl())
                .isEqualTo("https://img.youtube.com/vi/vidB/hqdefault.jpg");
        server.verify();
    }

    @Test
    void ante_error_de_la_api_lanza_YouTubeApiException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("/channels")))
                .andRespond(withServerError());

        YouTubeService service = new YouTubeService(builder, "api-key", "UC-canal");

        assertThatThrownBy(() -> service.fetchRecentUploads(6))
                .isInstanceOf(YouTubeApiException.class);
        server.verify();
    }
}
