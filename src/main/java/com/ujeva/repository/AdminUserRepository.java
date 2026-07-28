package com.ujeva.repository;

import com.ujeva.model.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a los usuarios del panel de administración.
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    /** Busca un usuario por su nombre de usuario (para la autenticación). */
    Optional<AdminUser> findByUsername(String username);

    /** Indica si ya existe un usuario con ese nombre (útil para el sembrado). */
    boolean existsByUsername(String username);
}
