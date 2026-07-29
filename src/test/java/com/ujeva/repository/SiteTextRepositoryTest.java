package com.ujeva.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ujeva.model.SiteText;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SiteTextRepositoryTest {

    @Autowired
    private SiteTextRepository repository;

    @Test
    void guardaYRecuperaPorClave() {
        repository.save(new SiteText("heroLema", "Todo por Dios & para Dios"));

        assertThat(repository.findByContentKey("heroLema"))
                .isPresent()
                .get()
                .satisfies(t -> {
                    assertThat(t.getContentValue()).isEqualTo("Todo por Dios & para Dios");
                    assertThat(t.getUpdatedAt()).isNotNull(); // @PrePersist
                });
        assertThat(repository.existsByContentKey("heroLema")).isTrue();
        assertThat(repository.findByContentKey("noExiste")).isEmpty();
    }

    @Test
    void rechazaClavesDuplicadas() {
        repository.saveAndFlush(new SiteText("aboutText", "uno"));

        assertThatThrownBy(() -> repository.saveAndFlush(new SiteText("aboutText", "dos")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
