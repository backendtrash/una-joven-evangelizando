package com.ujeva.controller;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.repository.CachedPostRepository;
import com.ujeva.service.SiteContentService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sitio público (renderizado en servidor).
 *
 * <p>Arma el modelo de la landing scrollytelling desde la base de datos: textos
 * editables, grilla de videos con el destacado fijado primero (F4), grilla de
 * oración (PRAYER), chips de formatos y párrafos de "Acerca de mí".
 */
@Controller
public class HomeController {

    private static final int GRID_MAX = 6;
    private static final int STEPS = 5;

    /** Chips de la escena 3 (fijos, verbatim del diseño). */
    private static final List<String> FORMATOS = List.of(
            "Reflexiones", "Enseñanzas", "Testimonios", "Lecturas de la Palabra",
            "Recomendaciones", "Vida de santos", "Muchas sorpresas más");

    private final SiteContentService siteContentService;
    private final CachedPostRepository cachedPostRepository;
    private final String socialYoutube;
    private final String socialInstagram;
    private final String socialTiktok;
    private final String socialFacebook;

    public HomeController(
            SiteContentService siteContentService,
            CachedPostRepository cachedPostRepository,
            @Value("${app.social.youtube}") String socialYoutube,
            @Value("${app.social.instagram}") String socialInstagram,
            @Value("${app.social.tiktok}") String socialTiktok,
            @Value("${app.social.facebook}") String socialFacebook) {
        this.siteContentService = siteContentService;
        this.cachedPostRepository = cachedPostRepository;
        this.socialYoutube = socialYoutube;
        this.socialInstagram = socialInstagram;
        this.socialTiktok = socialTiktok;
        this.socialFacebook = socialFacebook;
    }

    @GetMapping({"/", "/acerca"})
    public String home(Model model) {
        Map<String, String> content = siteContentService.loadContent();

        model.addAttribute("heroLema", content.get("heroLema"));
        model.addAttribute("heroPresentacion", content.get("heroPresentacion"));
        model.addAttribute("aboutParrafos",
                siteContentService.aboutParagraphs(content.get("aboutText")));
        model.addAttribute("gridVideos", buildGrid(content));
        model.addAttribute("prayVideos", buildPrayer());
        model.addAttribute("formatos", FORMATOS);
        model.addAttribute("dots", IntStream.range(0, STEPS).boxed().toList());
        model.addAttribute("social", socialLinks());
        return "index";
    }

    /** Grilla de la escena 1: video destacado fijado primero + recientes (máx. 6). */
    private List<VideoCard> buildGrid(Map<String, String> content) {
        List<CachedPost> recent =
                cachedPostRepository.findByTypeOrderByPublishedAtDesc(PostType.RECENT);
        String featuredId = siteContentService
                .resolveFeaturedVideoId(content.get("featuredUrl"), content.get("featuredId"))
                .orElse(null);

        List<VideoCard> cards = new ArrayList<>();
        if (featuredId != null) {
            CachedPost featuredPost = cachedPostRepository.findByVideoId(featuredId).orElse(null);
            cards.add(toCard(featuredId, featuredPost));
        }
        for (CachedPost post : recent) {
            if (cards.size() >= GRID_MAX) {
                break;
            }
            if (!post.getVideoId().equals(featuredId)) {
                cards.add(toCard(post.getVideoId(), post));
            }
        }
        return cards;
    }

    /** Grilla de la escena 4: videos de oración curados, por orden manual. */
    private List<VideoCard> buildPrayer() {
        return cachedPostRepository.findByTypeOrderBySortOrderAsc(PostType.PRAYER)
                .stream()
                .map(post -> toCard(post.getVideoId(), post))
                .toList();
    }

    private VideoCard toCard(String videoId, CachedPost post) {
        String thumbnail = (post != null && post.getThumbnailUrl() != null)
                ? post.getThumbnailUrl()
                : "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        String titulo = (post != null && post.getTitle() != null) ? post.getTitle() : "Video destacado";
        String meta = (post != null && post.getMeta() != null) ? post.getMeta() : "YouTube";
        String embed = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?autoplay=1&rel=0&playsinline=1";
        return new VideoCard(videoId, titulo, meta, thumbnail, embed);
    }

    private Map<String, String> socialLinks() {
        Map<String, String> social = new LinkedHashMap<>();
        social.put("youtube", socialYoutube);
        social.put("instagram", socialInstagram);
        social.put("tiktok", socialTiktok);
        social.put("facebook", socialFacebook);
        return social;
    }
}
