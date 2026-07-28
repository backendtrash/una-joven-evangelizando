package com.ujeva.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ujeva.model.AdminUser;
import com.ujeva.repository.AdminUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedAdmin() {
        if (!adminUserRepository.existsByUsername("maria")) {
            adminUserRepository.save(
                    new AdminUser("maria", passwordEncoder.encode("fiat2025")));
        }
    }

    @AfterEach
    void cleanup() {
        adminUserRepository.deleteAll();
    }

    @Test
    void accesoAnonimoAAdminRedirigeAlLogin() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/admin/login"));
    }

    @Test
    void paginaDeLoginEsPublica() throws Exception {
        mvc.perform(get("/admin/login"))
                .andExpect(status().isOk());
    }

    @Test
    void credencialesValidasAutentican() throws Exception {
        mvc.perform(post("/admin/login")
                        .param("username", "maria")
                        .param("password", "fiat2025")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(authenticated().withUsername("maria"));
    }

    @Test
    void credencialesInvalidasNoAutentican() throws Exception {
        mvc.perform(post("/admin/login")
                        .param("username", "maria")
                        .param("password", "incorrecta")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    void elHashBcryptCoincideConLaContrasena() {
        String hash = adminUserRepository.findByUsername("maria").orElseThrow().getPasswordHash();
        assertThat(hash).isNotEqualTo("fiat2025"); // almacenado hasheado (RA-01)
        assertThat(passwordEncoder.matches("fiat2025", hash)).isTrue();
    }
}
