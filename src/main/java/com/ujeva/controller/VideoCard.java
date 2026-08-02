package com.ujeva.controller;

/**
 * Modelo de vista de una tarjeta de video en las grillas del sitio.
 *
 * <p>Se expone con getters JavaBean para que Thymeleaf lo lea sin ambigüedad.
 * {@code embedUrl} usa {@code youtube-nocookie} con autoplay para el click-to-play
 * en escritorio; {@code watchUrl} es el enlace normal de YouTube, que en celular se
 * abre directamente (el reproductor embebido queda demasiado pequeño ahí).
 */
public class VideoCard {

    private final String videoId;
    private final String titulo;
    private final String meta;
    private final String thumbnailUrl;
    private final String embedUrl;
    private final String watchUrl;

    public VideoCard(String videoId, String titulo, String meta, String thumbnailUrl,
                     String embedUrl, String watchUrl) {
        this.videoId = videoId;
        this.titulo = titulo;
        this.meta = meta;
        this.thumbnailUrl = thumbnailUrl;
        this.embedUrl = embedUrl;
        this.watchUrl = watchUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMeta() {
        return meta;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getEmbedUrl() {
        return embedUrl;
    }

    public String getWatchUrl() {
        return watchUrl;
    }
}
