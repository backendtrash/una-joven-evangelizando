package com.ujeva.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @BeforeEach
    void seedVideo() {
        CachedPost post = new CachedPost("vidX", PostType.FEATURED);
        post.setTitle("Reflexión de prueba");
        post.setMeta("Reflexión · 8 min");
        post.setSortOrder(1);
        cachedPostRepository.save(post);
    }

    @AfterEach
    void cleanup() {
        cachedPostRepository.deleteAll();
    }

    @Test
    void laPortadaRenderizaLasCincoEscenasYElContenido() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-scene=\"0\"")))
                .andExpect(content().string(containsString("data-scene=\"4\"")))
                .andExpect(content().string(containsString("data-story")))
                // Textos sembrados (heroLema y heroPresentacion por defecto).
                .andExpect(content().string(containsString("Todo por Dios")))
                .andExpect(content().string(containsString("Soy una joven")))
                // Chips de formatos y encabezados de escena.
                .andExpect(content().string(containsString("Reflexiones")))
                .andExpect(content().string(containsString("Videos destacados")))
                .andExpect(content().string(containsString("Mi misión")))
                .andExpect(content().string(containsString("Aprendamos a")))
                // La tarjeta del video sembrado (destacado fijado primero).
                .andExpect(content().string(containsString("Reflexión de prueba")))
                .andExpect(content().string(containsString("youtube-nocookie.com/embed/vidX")))
                // Navegación de puntos.
                .andExpect(content().string(containsString("data-dot=\"0\"")));
    }

    @Test
    void laRutaAcercaTambienRenderizaLaPortada() throws Exception {
        mvc.perform(get("/acerca"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-story")));
    }

    @Test
    void renderizaAcercaEditableYFooter() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                // Sección "Acerca de mí" con el texto editable por defecto (párrafos).
                .andExpect(content().string(containsString("Acerca de mí")))
                .andExpect(content().string(containsString("nació del deseo")));
                
    }
}
