package com.ujeva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContentCacheServiceTest {

    @Autowired
    private CachedPostRepository repository;

    private final YouTubeService youTubeService = mock(YouTubeService.class);
    private ContentCacheService service;

    @BeforeEach
    void setUp() {
        service = new ContentCacheService(youTubeService, repository, 12, false);
    }

    @Test
    void anteFalloDeLaApiConservaLaCache() {
        repository.save(new CachedPost("reciente1", PostType.RECENT));
        repository.save(new CachedPost("oracion1", PostType.PRAYER));
        when(youTubeService.fetchRecentUploads(anyInt()))
                .thenThrow(new YouTubeApiException("caída", null));

        service.refreshRecent();

        assertThat(repository.count()).isEqualTo(2); // nada se elimina
    }

    @Test
    void respuestaVaciaConservaLaCache() {
        repository.save(new CachedPost("reciente1", PostType.RECENT));
        when(youTubeService.fetchRecentUploads(anyInt())).thenReturn(List.of());

        service.refreshRecent();

        assertThat(repository.findByVideoId("reciente1")).isPresent();
    }

    @Test
    void upsertNuevosYPruneDeRecientesObsoletosSinTocarPrayer() {
        repository.save(new CachedPost("viejo", PostType.RECENT));   // debe podarse
        repository.save(new CachedPost("oracion1", PostType.PRAYER)); // nunca se toca

        CachedPost fresco = new CachedPost("nuevo", PostType.RECENT);
        fresco.setTitle("Nuevo video");
        when(youTubeService.fetchRecentUploads(anyInt())).thenReturn(List.of(fresco));

        service.refreshRecent();

        assertThat(repository.findByVideoId("nuevo")).isPresent();  // upsert
        assertThat(repository.findByVideoId("viejo")).isEmpty();    // podado
        assertThat(repository.findByVideoId("oracion1")).isPresent(); // PRAYER preservado
    }

    @Test
    void noSobrescribeUnVideoCuradoComoPrayer() {
        // Un video ya curado como PRAYER no debe convertirse en RECENT ni duplicarse.
        repository.save(new CachedPost("compartido", PostType.PRAYER));

        CachedPost fresco = new CachedPost("compartido", PostType.RECENT);
        when(youTubeService.fetchRecentUploads(anyInt())).thenReturn(List.of(fresco));

        service.refreshRecent();

        assertThat(repository.findByVideoId("compartido"))
                .get()
                .satisfies(p -> assertThat(p.getType()).isEqualTo(PostType.PRAYER));
    }
}
