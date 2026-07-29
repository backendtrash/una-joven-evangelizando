package com.ujeva.service;

/**
 * Señala un fallo al consultar la YouTube Data API v3.
 *
 * <p>La captura el servicio de caché (T-12) para conservar el contenido ya
 * cacheado y que el sitio sobreviva a caídas de la API.
 */
public class YouTubeApiException extends RuntimeException {

    public YouTubeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
