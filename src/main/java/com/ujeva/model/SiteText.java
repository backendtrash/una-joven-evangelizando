package com.ujeva.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Fragmento editable de copia del sitio, identificado por una clave estable.
 *
 * <p>Contiene las 5 claves editables desde el panel: {@code heroLema},
 * {@code heroPresentacion}, {@code aboutText}, {@code featuredId} y
 * {@code featuredUrl}. El sitio público se renderiza a partir de estas filas.
 */
@Entity
@Table(name = "site_text")
public class SiteText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clave lógica del fragmento (única). */
    @Column(name = "content_key", nullable = false, unique = true, length = 64)
    private String contentKey;

    /** Valor del fragmento; puede ser texto largo (párrafos). */
    @Column(name = "content_value", columnDefinition = "TEXT")
    private String contentValue;

    /** Última modificación; se actualiza automáticamente al persistir. */
    @Column(name = "updated_at")
    private Instant updatedAt;

    protected SiteText() {
        // Requerido por JPA.
    }

    public SiteText(String contentKey, String contentValue) {
        this.contentKey = contentKey;
        this.contentValue = contentValue;
    }

    @PrePersist
    @PreUpdate
    private void touchUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getContentValue() {
        return contentValue;
    }

    public void setContentValue(String contentValue) {
        this.contentValue = contentValue;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
