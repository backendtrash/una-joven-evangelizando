package com.ujeva.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrayerAdminTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void agregaUnVideoDeOracion() throws Exception {
        mvc.perform(post("/admin/prayer/add").with(csrf())
                        .param("title", "Cómo rezar el Rosario paso a paso")
                        .param("meta", "Oración · Guía")
                        .param("url", "https://youtu.be/ROSARIO123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        assertThat(cachedPostRepository.findByVideoId("ROSARIO123"))
                .get()
                .satisfies(p -> {
                    assertThat(p.getType()).isEqualTo(PostType.PRAYER);
                    assertThat(p.getTitle()).contains("Rosario");
                });
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void eliminaUnVideoDeOracion() throws Exception {
        CachedPost post = new CachedPost("delme", PostType.PRAYER);
        post.setSortOrder(1);
        cachedPostRepository.save(post);

        mvc.perform(post("/admin/prayer/delete").with(csrf())
                        .param("id", post.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(cachedPostRepository.findByVideoId("delme")).isEmpty();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void reordenaVideosDeOracion() {
        CachedPost a = new CachedPost("vidA", PostType.PRAYER);
        a.setTitle("A");
        a.setSortOrder(1);
        CachedPost b = new CachedPost("vidB", PostType.PRAYER);
        b.setTitle("B");
        b.setSortOrder(2);
        cachedPostRepository.saveAll(List.of(a, b));

        try {
            mvc.perform(post("/admin/prayer/move").with(csrf())
                            .param("id", b.getId().toString())
                            .param("direction", "up"))
                    .andExpect(status().is3xxRedirection());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(cachedPostRepository.findByVideoId("vidB").orElseThrow().getSortOrder()).isEqualTo(1);
        assertThat(cachedPostRepository.findByVideoId("vidA").orElseThrow().getSortOrder()).isEqualTo(2);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void laEscena4MuestraLosVideosGestionados() throws Exception {
        CachedPost post = new CachedPost("prayVid", PostType.PRAYER);
        post.setTitle("Aprende a hacer oración mental");
        post.setMeta("Oración · 12 min");
        post.setSortOrder(1);
        cachedPostRepository.save(post);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aprende a hacer oración mental")))
                .andExpect(content().string(containsString("youtube-nocookie.com/embed/prayVid")));
    }

    @Test
    void requiereAutenticacion() throws Exception {
        mvc.perform(post("/admin/prayer/add").with(csrf())
                        .param("title", "x")
                        .param("url", "https://youtu.be/UNAUTH99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));

        assertThat(cachedPostRepository.findByVideoId("UNAUTH99")).isEmpty();
    }
}
