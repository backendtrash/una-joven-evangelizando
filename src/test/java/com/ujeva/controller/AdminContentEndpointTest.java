package com.ujeva.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ujeva.repository.SiteTextRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // revierte las escrituras para no contaminar la BD compartida de pruebas
class AdminContentEndpointTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SiteTextRepository siteTextRepository;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void guardaUnaClaveValida() throws Exception {
        mvc.perform(post("/admin/content").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"heroLema\",\"value\":\"Nuevo lema editado\"}"))
                .andExpect(status().isNoContent());

        assertThat(siteTextRepository.findByContentKey("heroLema"))
                .get()
                .satisfies(t -> assertThat(t.getContentValue()).isEqualTo("Nuevo lema editado"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void rechazaClaveFueraDeLaWhitelist() throws Exception {
        mvc.perform(post("/admin/content").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"role\",\"value\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void rechazaValorDemasiadoLargo() throws Exception {
        String enorme = "x".repeat(5001);
        mvc.perform(post("/admin/content").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"heroPresentacion\",\"value\":\"" + enorme + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bloqueaElAccesoNoAutenticado() throws Exception {
        mvc.perform(post("/admin/content").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"heroLema\",\"value\":\"x\"}"))
                .andExpect(status().is3xxRedirection());
    }
}
