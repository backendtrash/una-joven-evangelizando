package com.ujeva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificador de contraseñas de la aplicación.
 *
 * <p>Se expone como bean para que lo compartan el sembrado inicial (T-08) y la
 * configuración de seguridad (T-09). BCrypt aplica hash con sal (RA-01).
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
