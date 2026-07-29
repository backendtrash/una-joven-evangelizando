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
class FeaturedAdminTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void agregaUnVideoDestacado() throws Exception {
        mvc.perform(post("/admin/featured/add").with(csrf())
                        .param("title", "Mi testimonio: cómo volví a la fe")
                        .param("meta", "Testimonio · 18 min")
                        .param("url", "https://youtu.be/TESTIM123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        assertThat(cachedPostRepository.findByVideoId("TESTIM123"))
                .get()
                .satisfies(p -> assertThat(p.getType()).isEqualTo(PostType.FEATURED));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void laEscena1MuestraLosDestacadosGestionados() throws Exception {
        CachedPost post = new CachedPost("featVid", PostType.FEATURED);
        post.setTitle("Vivir el Adviento con María");
        post.setSortOrder(1);
        cachedPostRepository.save(post);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Vivir el Adviento con María")))
                .andExpect(content().string(containsString("youtube-nocookie.com/embed/featVid")));
    }

    @Test
    void requiereAutenticacion() throws Exception {
        mvc.perform(post("/admin/featured/add").with(csrf())
                        .param("title", "x")
                        .param("url", "https://youtu.be/NOAUTH99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));

        assertThat(cachedPostRepository.findByVideoId("NOAUTH99")).isEmpty();
    }
}
