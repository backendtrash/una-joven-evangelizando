package com.ujeva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CachedPostRepositoryTest {

    @Autowired
    private CachedPostRepository repository;

    @Test
    void guardaYRecuperaPorVideoId() {
        CachedPost post = new CachedPost("abc123", PostType.RECENT);
        post.setTitle("Reflexión del día");
        repository.save(post);

        assertThat(repository.findByVideoId("abc123"))
                .isPresent()
                .get()
                .satisfies(p -> assertThat(p.getTitle()).isEqualTo("Reflexión del día"));
        assertThat(repository.findByVideoId("noExiste")).isEmpty();
    }

    @Test
    void rechazaVideoIdDuplicado() {
        repository.saveAndFlush(new CachedPost("dup", PostType.RECENT));

        assertThatThrownBy(() -> repository.saveAndFlush(new CachedPost("dup", PostType.PRAYER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void filtraPorTipoYOrdena() {
        CachedPost reciente1 = new CachedPost("r1", PostType.RECENT);
        reciente1.setPublishedAt(Instant.parse("2026-01-01T00:00:00Z"));
        CachedPost reciente2 = new CachedPost("r2", PostType.RECENT);
        reciente2.setPublishedAt(Instant.parse("2026-06-01T00:00:00Z"));

        CachedPost oracion1 = new CachedPost("p1", PostType.PRAYER);
        oracion1.setSortOrder(2);
        CachedPost oracion2 = new CachedPost("p2", PostType.PRAYER);
        oracion2.setSortOrder(1);

        repository.saveAll(List.of(reciente1, reciente2, oracion1, oracion2));

        // RECENT por fecha descendente: el más nuevo primero.
        assertThat(repository.findByTypeOrderByPublishedAtDesc(PostType.RECENT))
                .extracting(CachedPost::getVideoId)
                .containsExactly("r2", "r1");

        // PRAYER por orden manual ascendente.
        assertThat(repository.findByTypeOrderBySortOrderAsc(PostType.PRAYER))
                .extracting(CachedPost::getVideoId)
                .containsExactly("p2", "p1");
    }
}
