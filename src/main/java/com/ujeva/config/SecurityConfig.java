package com.ujeva.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad del sitio.
 *
 * <ul>
 *   <li>RA-02: {@code /admin/**} requiere autenticación; el resto del sitio es
 *       público. Un acceso anónimo a {@code /admin} redirige al login.</li>
 *   <li>Login por formulario con ruta propia {@code /admin/login}; logout en
 *       {@code /admin/logout}.</li>
 *   <li>CSRF habilitado (por defecto) para proteger los POST del panel (RA-03,
 *       decisión D).</li>
 * </ul>
 *
 * <p>El {@code PasswordEncoder} (BCrypt) y el {@code UserDetailsService} sobre la
 * base de datos se auto-cablean como proveedor de autenticación DAO.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos y páginas públicas.
                        .requestMatchers(
                                "/", "/acerca", "/admin/login",
                                "/css/**", "/js/**", "/img/**", "/images/**",
                                "/assets/**", "/favicon.ico", "/webjars/**").permitAll()
                        // Todo lo demás bajo /admin exige sesión autenticada.
                        .requestMatchers("/admin/**").authenticated()
                        // El resto del sitio público es abierto.
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                        .failureUrl("/admin/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());
        // CSRF queda habilitado por defecto.
        return http.build();
    }
}
