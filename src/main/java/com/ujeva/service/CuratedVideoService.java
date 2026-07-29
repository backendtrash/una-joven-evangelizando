package com.ujeva.service;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de videos curados por la administradora (decisión G, ampliada).
 *
 * <p>Sirve tanto para los {@link PostType#FEATURED} (grilla "Videos destacados",
 * escena 1) como para los {@link PostType#PRAYER} (escena 4). Ambos son curados: el
 * scheduler nunca los toca y se ordenan por {@code sortOrder}. La lógica es idéntica;
 * solo cambia el tipo.
 */
@Service
public class CuratedVideoService {

    private final CachedPostRepository cachedPostRepository;
    private final SiteContentService siteContentService;

    public CuratedVideoService(
            CachedPostRepository cachedPostRepository,
            SiteContentService siteContentService) {
        this.cachedPostRepository = cachedPostRepository;
        this.siteContentService = siteContentService;
    }

    /** Lista los videos curados de un tipo, en su orden manual. */
    public List<CachedPost> list(PostType type) {
        return cachedPostRepository.findByTypeOrderBySortOrderAsc(type);
    }

    /**
     * Agrega un video curado del tipo dado a partir de un enlace de YouTube.
     * Devuelve {@code true} si el enlace tenía un id válido y se guardó.
     */
    @Transactional
    public boolean add(PostType type, String title, String meta, String youtubeUrl) {
        String videoId = siteContentService.ytId(youtubeUrl).orElse(null);
        if (videoId == null) {
            return false;
        }
        int nextOrder = list(type).stream()
                .map(CachedPost::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        // Si el video ya existe (p. ej. estaba como RECENT), se reutiliza la fila.
        CachedPost post = cachedPostRepository.findByVideoId(videoId)
                .orElseGet(() -> new CachedPost(videoId, type));
        post.setType(type);
        post.setPlatform("YouTube");
        post.setTitle(title);
        post.setMeta(meta);
        post.setThumbnailUrl("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
        if (post.getSortOrder() == null) {
            post.setSortOrder(nextOrder);
        }
        post.setFetchedAt(Instant.now());
        cachedPostRepository.save(post);
        return true;
    }

    /** Elimina un video curado por id (solo si es del tipo indicado). */
    @Transactional
    public void remove(PostType type, Long id) {
        cachedPostRepository.findById(id)
                .filter(post -> post.getType() == type)
                .ifPresent(cachedPostRepository::delete);
    }

    /** Mueve un video del tipo dado hacia arriba o abajo intercambiando su orden. */
    @Transactional
    public void move(PostType type, Long id, boolean up) {
        List<CachedPost> ordered = list(type);
        int index = indexOf(ordered, id);
        if (index < 0) {
            return;
        }
        int neighbor = up ? index - 1 : index + 1;
        if (neighbor < 0 || neighbor >= ordered.size()) {
            return;
        }
        CachedPost a = ordered.get(index);
        CachedPost b = ordered.get(neighbor);
        Integer tmp = a.getSortOrder();
        a.setSortOrder(b.getSortOrder());
        b.setSortOrder(tmp);
        cachedPostRepository.save(a);
        cachedPostRepository.save(b);
    }

    private int indexOf(List<CachedPost> posts, Long id) {
        for (int i = 0; i < posts.size(); i++) {
            if (Objects.equals(posts.get(i).getId(), id)) {
                return i;
            }
        }
        return -1;
    }
}
