package com.ujeva.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del autoguardado del panel: una clave editable y su nuevo valor.
 *
 * <p>La clave debe pertenecer a la whitelist (se valida en el controlador) y el
 * valor tiene un tope de longitud (RA-03).
 */
public record ContentUpdate(
        @NotBlank @Size(max = 64) String key,
        @Size(max = 5000) String value) {
}
