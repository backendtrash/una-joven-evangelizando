package com.ujeva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.model.SiteText;
import com.ujeva.repository.CachedPostRepository;
import com.ujeva.repository.SiteTextRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteContentServiceTest {

    @Mock
    private SiteTextRepository siteTextRepository;

    @Mock
    private CachedPostRepository cachedPostRepository;

    @InjectMocks
    private SiteContentService service;

    @Test
    void ytIdExtraeDeLasFormasComunes() {
        assertThat(service.ytId("https://youtu.be/ScMzIvxBSi4")).contains("ScMzIvxBSi4");
        assertThat(service.ytId("https://www.youtube.com/watch?v=ScMzIvxBSi4")).contains("ScMzIvxBSi4");
        assertThat(service.ytId("https://www.youtube.com/embed/ScMzIvxBSi4")).contains("ScMzIvxBSi4");
        assertThat(service.ytId("https://youtube.com/shorts/ScMzIvxBSi4")).contains("ScMzIvxBSi4");
        assertThat(service.ytId("https://example.com/nada")).isEmpty();
        assertThat(service.ytId(null)).isEmpty();
    }

    @Test
    void aboutParagraphsDivideEnParrafos() {
        List<String> parrafos = service.aboutParagraphs("Primero.\n\nSegundo.\n\n  \n\nTercero.");
        assertThat(parrafos).containsExactly("Primero.", "Segundo.", "Tercero.");
        assertThat(service.aboutParagraphs("   ")).isEmpty();
        assertThat(service.aboutParagraphs(null)).isEmpty();
    }

    @Test
    void featuredUrlTienePrioridadSobreTodo() {
        // La URL gana aunque exista featuredId y videos recientes.
        Optional<String> id = service.resolveFeaturedVideoId(
                "https://youtu.be/URL1234", "otroId");
        assertThat(id).contains("URL1234");
    }

    @Test
    void featuredIdSeUsaCuandoNoHayUrl() {
        Optional<String> id = service.resolveFeaturedVideoId("", "elegido99");
        assertThat(id).contains("elegido99");
    }

    @Test
    void caeAlMasRecienteCuandoNoHayUrlNiId() {
        CachedPost reciente = new CachedPost("reciente1", PostType.RECENT);
        when(cachedPostRepository.findByTypeOrderByPublishedAtDesc(PostType.RECENT))
                .thenReturn(List.of(reciente));

        Optional<String> id = service.resolveFeaturedVideoId(null, null);
        assertThat(id).contains("reciente1");
    }

    @Test
    void sinUrlIdNiRecientesDevuelveVacio() {
        when(cachedPostRepository.findByTypeOrderByPublishedAtDesc(PostType.RECENT))
                .thenReturn(List.of());

        assertThat(service.resolveFeaturedVideoId(null, "")).isEmpty();
    }

    @Test
    void loadContentTraeLas5ClavesYRellenaVacias() {
        // Solo heroLema existe; las otras 4 claves devuelven Optional vacío -> "".
        when(siteTextRepository.findByContentKey("heroLema"))
                .thenReturn(Optional.of(new SiteText("heroLema", "Lema")));

        var content = service.loadContent();
        assertThat(content).hasSize(5)
                .containsEntry("heroLema", "Lema")
                .containsEntry("aboutText", "")
                .containsEntry("featuredUrl", "");
    }
}
