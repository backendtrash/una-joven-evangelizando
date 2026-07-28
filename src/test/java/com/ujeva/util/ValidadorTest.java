package com.ujeva.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidadorTest {

    @Test
    @DisplayName("noVacio: null y espacios en blanco son vacíos; texto real no lo es")
    void noVacio() {
        assertThat(Validador.noVacio(null)).isFalse();
        assertThat(Validador.noVacio("")).isFalse();
        assertThat(Validador.noVacio("   ")).isFalse();
        assertThat(Validador.noVacio("  Fiat  ")).isTrue();
    }

    @Test
    @DisplayName("dentroDeLargo: respeta el límite y trata null como largo 0")
    void dentroDeLargo() {
        assertThat(Validador.dentroDeLargo(null, 5)).isTrue();
        assertThat(Validador.dentroDeLargo("hola", 5)).isTrue();
        assertThat(Validador.dentroDeLargo("hola!", 5)).isTrue();
        assertThat(Validador.dentroDeLargo("holaaa", 5)).isFalse();
    }

    @Test
    @DisplayName("dentroDeLargo: un máximo negativo es un error de programación")
    void dentroDeLargoRechazaMaximoNegativo() {
        assertThatThrownBy(() -> Validador.dentroDeLargo("x", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
