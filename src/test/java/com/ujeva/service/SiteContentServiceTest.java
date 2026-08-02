package com.ujeva.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ujeva.model.SiteText;
import com.ujeva.repository.SiteTextRepository;
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
    void isEditableKeyRespetaLaWhitelist() {
        assertThat(service.isEditableKey("heroLema")).isTrue();
        assertThat(service.isEditableKey("heroPresentacion")).isTrue();
        assertThat(service.isEditableKey("aboutText")).isFalse();
        assertThat(service.isEditableKey("featuredId")).isFalse();
        assertThat(service.isEditableKey("role")).isFalse();
    }

    @Test
    void loadContentTraeLasClavesYRellenaVacias() {
        // Solo heroLema existe; la otra clave devuelve Optional vacío -> "".
        when(siteTextRepository.findByContentKey("heroLema"))
                .thenReturn(Optional.of(new SiteText("heroLema", "Lema")));

        var content = service.loadContent();
        assertThat(content).hasSize(2)
                .containsEntry("heroLema", "Lema")
                .containsEntry("heroPresentacion", "");
    }
}
