package com.ujeva.repository;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a las publicaciones de video cacheadas.
 */
public interface CachedPostRepository extends JpaRepository<CachedPost, Long> {

    /** Busca una publicación por su id de video (clave natural única). */
    Optional<CachedPost> findByVideoId(String videoId);

    /** Publicaciones de un tipo, ordenadas por fecha de publicación (más recientes primero). */
    List<CachedPost> findByTypeOrderByPublishedAtDesc(PostType type);

    /** Publicaciones de un tipo, ordenadas por el orden manual (para las filas PRAYER). */
    List<CachedPost> findByTypeOrderBySortOrderAsc(PostType type);
}
