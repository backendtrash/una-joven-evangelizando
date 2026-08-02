package com.ujeva.controller;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.service.CuratedVideoService;
import com.ujeva.service.SiteContentService;
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
 * editables, grilla "Videos destacados" (lista curada FEATURED), grilla de oración
 * (lista curada PRAYER), chips de formatos y párrafos de "Acerca de mí".
 */
@Controller
public class HomeController {

    private static final int STEPS = 5;

    /** Chips de la escena 3 (fijos, verbatim del diseño). */
    private static final List<String> FORMATOS = List.of(
            "Reflexiones", "Enseñanzas", "Testimonios", "Lecturas de la Palabra",
            "Recomendaciones", "Vida de santos", "Muchas sorpresas más");

    private final SiteContentService siteContentService;
    private final CuratedVideoService curatedVideoService;
    private final String socialYoutube;
    private final String socialInstagram;
    private final String socialTiktok;
    private final String socialFacebook;

    public HomeController(
            SiteContentService siteContentService,
            CuratedVideoService curatedVideoService,
            @Value("${app.social.youtube}") String socialYoutube,
            @Value("${app.social.instagram}") String socialInstagram,
            @Value("${app.social.tiktok}") String socialTiktok,
            @Value("${app.social.facebook}") String socialFacebook) {
        this.siteContentService = siteContentService;
        this.curatedVideoService = curatedVideoService;
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
        model.addAttribute("gridVideos", toCards(curatedVideoService.list(PostType.FEATURED)));
        model.addAttribute("prayVideos", toCards(curatedVideoService.list(PostType.PRAYER)));
        model.addAttribute("formatos", FORMATOS);
        model.addAttribute("dots", IntStream.range(0, STEPS).boxed().toList());
        model.addAttribute("social", socialLinks());
        return "index";
    }

    private List<VideoCard> toCards(List<CachedPost> posts) {
        return posts.stream().map(this::toCard).toList();
    }

    private VideoCard toCard(CachedPost post) {
        String videoId = post.getVideoId();
        String thumbnail = (post.getThumbnailUrl() != null)
                ? post.getThumbnailUrl()
                : "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        String embed = "https://www.youtube-nocookie.com/embed/" + videoId
                + "?autoplay=1&rel=0&playsinline=1";
        String watch = "https://www.youtube.com/watch?v=" + videoId;
        return new VideoCard(videoId, post.getTitle(), post.getMeta(), thumbnail, embed, watch);
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
