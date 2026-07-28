package com.ujeva.config;

import com.ujeva.model.AdminUser;
import com.ujeva.model.SiteText;
import com.ujeva.repository.AdminUserRepository;
import com.ujeva.repository.SiteTextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Siembra datos iniciales de forma idempotente al arrancar.
 *
 * <p>Inserta las 5 filas por defecto de {@code site_text} y un usuario admin (con
 * contraseña hasheada por BCrypt) solo si aún no existen. Ejecutarlo varias veces
 * no duplica datos. Los textos por defecto se toman verbatim del prototipo de
 * diseño; el valor de {@code featuredId}/{@code featuredUrl} queda vacío para que
 * el video destacado recaiga en "el más reciente" hasta que la admin elija uno.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // --- Copia por defecto (verbatim del diseño). El lema usa "&" por diseño. ---
    static final String DEFAULT_HERO_LEMA =
            "Todo por Dios & para Dios, guiada por el Espíritu Santo, junto a Mamá María";

    static final String DEFAULT_HERO_PRESENTACION =
            "Soy una joven católica que comparte su testimonio de fe a través de las "
            + "redes sociales por medio de reflexiones, testimonios, rosarios, "
            + "recomendaciones, la Palabra de Dios y sobre todo la esperanza y el amor "
            + "de Dios.";

    static final String DEFAULT_ABOUT_TEXT =
            "«Una Joven Evangelizando» nació del deseo de llevar el Evangelio al lugar "
            + "donde hoy vive la juventud: las redes sociales. Cada video, cada "
            + "publicación y cada oración compartida busca lo mismo — que descubras que "
            + "Dios te ama y que no caminas solo.\n\n"
            + "Creo en una fe alegre, cercana y luminosa, vivida de la mano de María. "
            + "Aquí encontrarás contenido para tu día a día: reflexiones para rezar por "
            + "la mañana, el rosario para acompañarte, y respuestas sencillas a tus "
            + "preguntas sobre la fe.";

    private final SiteTextRepository siteTextRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(
            SiteTextRepository siteTextRepository,
            AdminUserRepository adminUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.initial-username:maria}") String adminUsername,
            @Value("${app.admin.initial-password:}") String adminPassword) {
        this.siteTextRepository = siteTextRepository;
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        seedSiteText();
        seedAdminUser();
    }

    private void seedSiteText() {
        seedText("heroLema", DEFAULT_HERO_LEMA);
        seedText("heroPresentacion", DEFAULT_HERO_PRESENTACION);
        seedText("aboutText", DEFAULT_ABOUT_TEXT);
        seedText("featuredId", "");
        seedText("featuredUrl", "");
    }

    private void seedText(String key, String value) {
        if (!siteTextRepository.existsByContentKey(key)) {
            siteTextRepository.save(new SiteText(key, value));
            log.info("Sembrado site_text '{}'.", key);
        }
    }

    private void seedAdminUser() {
        if (adminUserRepository.existsByUsername(adminUsername)) {
            return;
        }
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn(
                    "ADMIN_INITIAL_PASSWORD no está definido: no se sembró el usuario "
                    + "admin '{}'. Define la variable de entorno para crearlo.",
                    adminUsername);
            return;
        }
        adminUserRepository.save(
                new AdminUser(adminUsername, passwordEncoder.encode(adminPassword)));
        log.info("Usuario admin '{}' sembrado.", adminUsername);
    }
}
