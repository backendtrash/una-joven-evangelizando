package com.ujeva.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ujeva.repository.CachedPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmptyStateTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CachedPostRepository cachedPostRepository;

    @Test
    void laPortadaConCacheVaciaMuestraEstadosVacios() throws Exception {
        cachedPostRepository.deleteAll();

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pronto encontrarás aquí los videos")))
                .andExpect(content().string(containsString("Pronto encontrarás aquí videos para aprender a rezar")));
    }

    @Test
    void unaRutaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/ruta-que-no-existe"))
                .andExpect(status().isNotFound());
    }
}
