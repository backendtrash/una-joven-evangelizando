package com.ujeva.service;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de los videos de oración (escena 4), curados por la administradora
 * (decisión G). Son {@link PostType#PRAYER}: el scheduler nunca los toca y se
 * ordenan por {@code sortOrder}.
 */
@Service
public class PrayerVideoService {

    private final CachedPostRepository cachedPostRepository;
    private final SiteContentService siteContentService;

    public PrayerVideoService(
            CachedPostRepository cachedPostRepository,
            SiteContentService siteContentService) {
        this.cachedPostRepository = cachedPostRepository;
        this.siteContentService = siteContentService;
    }

    /** Lista los videos de oración en su orden manual. */
    public List<CachedPost> list() {
        return cachedPostRepository.findByTypeOrderBySortOrderAsc(PostType.PRAYER);
    }

    /**
     * Agrega un video de oración a partir de un enlace de YouTube. Devuelve
     * {@code true} si el enlace tenía un id válido y se guardó.
     */
    @Transactional
    public boolean add(String title, String meta, String youtubeUrl) {
        String videoId = siteContentService.ytId(youtubeUrl).orElse(null);
        if (videoId == null) {
            return false;
        }
        int nextOrder = list().stream()
                .map(CachedPost::getSortOrder)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        // Si el video ya existe (p. ej. estaba como RECENT), se reutiliza la fila.
        CachedPost post = cachedPostRepository.findByVideoId(videoId)
                .orElseGet(() -> new CachedPost(videoId, PostType.PRAYER));
        post.setType(PostType.PRAYER);
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

    /** Elimina un video de oración por id (solo si es de tipo PRAYER). */
    @Transactional
    public void remove(Long id) {
        cachedPostRepository.findById(id)
                .filter(post -> post.getType() == PostType.PRAYER)
                .ifPresent(cachedPostRepository::delete);
    }

    /** Mueve un video de oración hacia arriba o abajo intercambiando su orden. */
    @Transactional
    public void move(Long id, boolean up) {
        List<CachedPost> ordered = list();
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

    /** Utilidad para la vista previa del enlace en el panel. */
    public Optional<String> extractVideoId(String url) {
        return siteContentService.ytId(url);
    }
}
