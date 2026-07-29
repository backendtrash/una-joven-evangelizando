package com.ujeva.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.ujeva.repository.AdminUserRepository;
import com.ujeva.repository.SiteTextRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DataSeederTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    private SiteTextRepository siteTextRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Test
    void siembraDefaultsYAdminDeFormaIdempotente() {
        DataSeeder seeder = new DataSeeder(
                siteTextRepository, adminUserRepository, encoder, "maria", "fiat2025");

        seeder.run();
        seeder.run(); // ejecutarlo dos veces no debe duplicar nada

        assertThat(siteTextRepository.count()).isEqualTo(5);
        assertThat(siteTextRepository.findByContentKey("heroLema"))
                .get()
                .satisfies(t -> assertThat(t.getContentValue()).contains("Todo por Dios"));
        assertThat(siteTextRepository.findByContentKey("featuredUrl"))
                .get()
                .satisfies(t -> assertThat(t.getContentValue()).isEmpty());

        assertThat(adminUserRepository.count()).isEqualTo(1);
        assertThat(adminUserRepository.findByUsername("maria"))
                .isPresent()
                .get()
                .satisfies(u -> {
                    // La contraseña se almacena hasheada, nunca en texto plano (RA-01).
                    assertThat(u.getPasswordHash()).isNotEqualTo("fiat2025");
                    assertThat(encoder.matches("fiat2025", u.getPasswordHash())).isTrue();
                });
    }

    @Test
    void sinContrasenaNoSiembraAdminPeroSiLosTextos() {
        DataSeeder seeder = new DataSeeder(
                siteTextRepository, adminUserRepository, encoder, "maria", "");

        seeder.run();

        assertThat(siteTextRepository.count()).isEqualTo(5);
        assertThat(adminUserRepository.count()).isZero();
    }
}
