package com.ujeva.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminDashboardTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void elTableroRequiereAutenticacion() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "maria", roles = {"ADMIN"})
    void elTableroMuestraLosValoresActuales() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk())
                // Estructura del tablero.
                .andExpect(content().string(containsString("Administración")))
                .andExpect(content().string(containsString("Portada")))
                .andExpect(content().string(containsString("Videos destacados")))
                .andExpect(content().string(containsString("Videos de oración")))
                // Campos con data-field para el autoguardado.
                .andExpect(content().string(containsString("data-field=\"heroLema\"")))
                .andExpect(content().string(containsString("data-field=\"heroPresentacion\"")))
                // Valor actual precargado (heroLema sembrado por defecto).
                .andExpect(content().string(containsString("Todo por Dios")))
                // Token CSRF disponible para el fetch de autoguardado.
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }
}
