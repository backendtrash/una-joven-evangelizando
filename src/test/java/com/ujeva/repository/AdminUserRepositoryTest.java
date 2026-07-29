package com.ujeva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ujeva.model.AdminUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AdminUserRepositoryTest {

    @Autowired
    private AdminUserRepository repository;

    @Test
    void guardaYRecuperaPorUsername() {
        repository.save(new AdminUser("maria", "$2a$10$hashficticio"));

        assertThat(repository.findByUsername("maria"))
                .isPresent()
                .get()
                .satisfies(u -> {
                    assertThat(u.isEnabled()).isTrue();
                    assertThat(u.getRole()).isEqualTo("ADMIN");
                });
        assertThat(repository.existsByUsername("maria")).isTrue();
        assertThat(repository.findByUsername("otro")).isEmpty();
    }

    @Test
    void rechazaUsernameDuplicado() {
        repository.saveAndFlush(new AdminUser("maria", "hash1"));

        assertThatThrownBy(() -> repository.saveAndFlush(new AdminUser("maria", "hash2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
