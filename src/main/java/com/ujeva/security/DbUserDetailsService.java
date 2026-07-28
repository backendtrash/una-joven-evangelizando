package com.ujeva.security;

import com.ujeva.model.AdminUser;
import com.ujeva.repository.AdminUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga los usuarios del panel desde la base de datos para Spring Security.
 *
 * <p>El hash BCrypt almacenado se compara contra la contraseña ingresada mediante
 * el {@code PasswordEncoder} configurado; aquí nunca se maneja texto plano.
 */
@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public DbUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .disabled(!user.isEnabled())
                .roles(user.getRole()) // "ADMIN" -> autoridad ROLE_ADMIN
                .build();
    }
}
