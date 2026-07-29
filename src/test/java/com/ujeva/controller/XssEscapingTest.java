package com.ujeva.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verifica que el contenido controlado por el usuario se renderiza escapado (RA-03):
 * un payload con &lt;script&gt; guardado en aboutText aparece escapado en la portada,
 * no como una etiqueta ejecutable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class XssEscapingTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void elPayloadScriptEnAboutTextSeRenderizaEscapado() throws Exception {
        String payload = "<script>alert('xss')</script>Hola de nuevo";

        mvc.perform(post("/admin/content").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"aboutText\",\"value\":\"" + payload + "\"}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                // El payload aparece escapado…
                .andExpect(content().string(containsString("&lt;script&gt;alert")))
                // …y nunca como etiqueta <script> ejecutable con el alert.
                .andExpect(content().string(not(containsString("<script>alert('xss')"))));
    }
}
