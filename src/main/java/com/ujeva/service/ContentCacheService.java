package com.ujeva.service;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantiene la caché de contenido reciente de YouTube.
 *
 * <p>Refresca al arranque y una vez al día. En cada refresco hace upsert de los
 * videos {@link PostType#RECENT} obtenidos y elimina los RECENT que ya no aparecen;
 * los {@link PostType#PRAYER} (curados por la admin) nunca se tocan. Si la API falla
 * o no devuelve videos, se conserva la caché existente para que el sitio sobreviva a
 * caídas de la API (F3).
 */
@Service
public class ContentCacheService {

    private static final Logger log = LoggerFactory.getLogger(ContentCacheService.class);

    private final YouTubeService youTubeService;
    private final CachedPostRepository cachedPostRepository;
    private final int maxResults;
    private final boolean refreshOnStartup;

    public ContentCacheService(
            YouTubeService youTubeService,
            CachedPostRepository cachedPostRepository,
            @Value("${youtube.recent.max:12}") int maxResults,
            @Value("${app.cache.refresh-on-startup:true}") boolean refreshOnStartup) {
        this.youTubeService = youTubeService;
        this.cachedPostRepository = cachedPostRepository;
        this.maxResults = maxResults;
        this.refreshOnStartup = refreshOnStartup;
    }

    /** Refresco al arranque (deshabilitable con app.cache.refresh-on-startup=false). */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!refreshOnStartup) {
            log.info("Refresco de caché al arranque deshabilitado.");
            return;
        }
        refreshRecent();
    }

    /** Refresco diario (por defecto 04:00). */
    @Scheduled(cron = "${app.cache.refresh-cron:0 0 4 * * *}")
    public void scheduledRefresh() {
        refreshRecent();
    }

    /**
     * Obtiene los videos recientes y actualiza la caché RECENT (upsert + prune).
     * Ante fallo de la API o respuesta vacía, conserva la caché actual.
     */
    @Transactional
    public void refreshRecent() {
        List<CachedPost> fetched;
        try {
            fetched = youTubeService.fetchRecentUploads(maxResults);
        } catch (YouTubeApiException ex) {
            log.warn("No se pudo refrescar desde YouTube; se conserva la caché existente.", ex);
            return;
        }

        if (fetched.isEmpty()) {
            log.info("YouTube no devolvió videos; se conserva la caché existente.");
            return;
        }

        Set<String> freshIds = new HashSet<>();
        for (CachedPost fresh : fetched) {
            freshIds.add(fresh.getVideoId());
            upsert(fresh);
        }
        pruneStaleRecent(freshIds);
        log.info("Caché RECENT refrescada: {} videos.", freshIds.size());
    }

    private void upsert(CachedPost fresh) {
        cachedPostRepository.findByVideoId(fresh.getVideoId()).ifPresentOrElse(existing -> {
            // No sobrescribir un video curado como PRAYER si comparte el mismo id.
            if (existing.getType() == PostType.PRAYER) {
                return;
            }
            existing.setPlatform(fresh.getPlatform());
            existing.setTitle(fresh.getTitle());
            existing.setThumbnailUrl(fresh.getThumbnailUrl());
            existing.setPublishedAt(fresh.getPublishedAt());
            existing.setFetchedAt(fresh.getFetchedAt());
            cachedPostRepository.save(existing);
        }, () -> cachedPostRepository.save(fresh));
    }

    /** Elimina los RECENT que ya no vienen en el refresco; nunca toca los PRAYER. */
    private void pruneStaleRecent(Set<String> freshIds) {
        List<CachedPost> existingRecent =
                cachedPostRepository.findByTypeOrderByPublishedAtDesc(PostType.RECENT);
        for (CachedPost existing : existingRecent) {
            if (!freshIds.contains(existing.getVideoId())) {
                cachedPostRepository.delete(existing);
            }
        }
    }
}
