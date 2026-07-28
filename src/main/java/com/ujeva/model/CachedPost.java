package com.ujeva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Publicación de video cacheada que alimenta las grillas del sitio.
 *
 * <p>El {@link #type} distingue las filas auto-obtenidas de YouTube ({@code RECENT},
 * refrescadas por el scheduler) de las curadas por la administradora ({@code PRAYER},
 * escena 4). La miniatura puede derivarse del id de YouTube sin consumir cuota:
 * {@code https://img.youtube.com/vi/<videoId>/hqdefault.jpg}.
 */
@Entity
@Table(
        name = "cached_post",
        indexes = {
                @Index(name = "idx_cached_post_published_at", columnList = "published_at DESC"),
                @Index(name = "idx_cached_post_type", columnList = "type")
        }
)
public class CachedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id del video en la plataforma (único). */
    @Column(name = "video_id", nullable = false, unique = true, length = 64)
    private String videoId;

    /** Plataforma de origen (p. ej. "YouTube"). */
    @Column(length = 32)
    private String platform;

    /** Origen/ciclo de vida de la fila (RECENT auto, PRAYER curada). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PostType type;

    @Column(length = 200)
    private String title;

    /** Línea de metadatos mostrada bajo el título (p. ej. "Oración · 12 min"). */
    @Column(length = 120)
    private String meta;

    @Column(name = "thumbnail_url", length = 300)
    private String thumbnailUrl;

    @Column(name = "published_at")
    private Instant publishedAt;

    /** Orden manual para las filas PRAYER (menor = primero). */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** Momento en que se cacheó/actualizó la fila. */
    @Column(name = "fetched_at")
    private Instant fetchedAt;

    protected CachedPost() {
        // Requerido por JPA.
    }

    public CachedPost(String videoId, PostType type) {
        this.videoId = videoId;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
