package com.ujeva.repository;

import com.ujeva.model.SiteText;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a los fragmentos editables de copia del sitio.
 */
public interface SiteTextRepository extends JpaRepository<SiteText, Long> {

    /** Busca un fragmento por su clave lógica. */
    Optional<SiteText> findByContentKey(String contentKey);

    /** Indica si ya existe un fragmento con esa clave (útil para el sembrado). */
    boolean existsByContentKey(String contentKey);
}
