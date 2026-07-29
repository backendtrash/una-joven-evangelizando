package com.ujeva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicación.
 *
 * <p>Sitio público (Spring MVC + Thymeleaf, renderizado en servidor) que presenta
 * a Una Joven Evangelizando y agrega su contenido reciente de YouTube, con un panel
 * de administración protegido para editar textos y elegir el video destacado.
 *
 * <p>{@code @EnableScheduling} habilita el refresco diario de la caché de contenido.
 */
@SpringBootApplication
@EnableScheduling
public class UnaJovenEvangelizandoApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnaJovenEvangelizandoApplication.class, args);
    }
}
