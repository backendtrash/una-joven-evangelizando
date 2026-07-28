package com.ujeva;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica que el contexto completo de Spring arranca bajo el perfil de pruebas
 * (base de datos H2 en memoria), sin requerir secretos ni conexión a Supabase.
 */
@SpringBootTest
@ActiveProfiles("test")
class UnaJovenEvangelizandoApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto no cargara, esta prueba fallaría al inicializarse.
    }
}
