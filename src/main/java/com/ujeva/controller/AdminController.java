package com.ujeva.controller;

import com.ujeva.model.CachedPost;
import com.ujeva.repository.CachedPostRepository;
import com.ujeva.service.PrayerVideoService;
import com.ujeva.service.SiteContentService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Panel de administración (protegido por Spring Security).
 *
 * <p>Sirve la página de login y el tablero de edición. Los endpoints de guardado y
 * de videos de oración se agregan en T-19 y T-20. Todo {@code /admin/**} requiere
 * autenticación.
 */
@Controller
public class AdminController {

    private final SiteContentService siteContentService;
    private final CachedPostRepository cachedPostRepository;
    private final PrayerVideoService prayerVideoService;

    public AdminController(
            SiteContentService siteContentService,
            CachedPostRepository cachedPostRepository,
            PrayerVideoService prayerVideoService) {
        this.siteContentService = siteContentService;
        this.cachedPostRepository = cachedPostRepository;
        this.prayerVideoService = prayerVideoService;
    }

    /** Página de inicio de sesión (Spring Security la usa como loginPage). */
    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }

    /** Tablero de edición, precargado con el contenido actual. */
    @GetMapping("/admin")
    public String dashboard(Model model) {
        Map<String, String> content = siteContentService.loadContent();
        model.addAttribute("content", content);
        model.addAttribute("posts", cachedPostRepository.findAll());

        // Vista previa del destacado actual (resuelto por precedencia F4).
        String featuredVideoId = siteContentService
                .resolveFeaturedVideoId(content.get("featuredUrl"), content.get("featuredId"))
                .orElse(null);
        CachedPost featured = (featuredVideoId == null)
                ? null
                : cachedPostRepository.findByVideoId(featuredVideoId).orElse(null);
        model.addAttribute("featured", featured);
        model.addAttribute("featuredVideoId", featuredVideoId);
        model.addAttribute("prayerVideos", prayerVideoService.list());
        return "admin/dashboard";
    }

    // --- Videos de oración (decisión G): endpoints autenticados y con CSRF. ---

    @PostMapping("/admin/prayer/add")
    public String addPrayer(
            @RequestParam String title,
            @RequestParam(required = false) String meta,
            @RequestParam String url) {
        prayerVideoService.add(title, meta, url);
        return "redirect:/admin";
    }

    @PostMapping("/admin/prayer/delete")
    public String deletePrayer(@RequestParam Long id) {
        prayerVideoService.remove(id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/prayer/move")
    public String movePrayer(@RequestParam Long id, @RequestParam String direction) {
        prayerVideoService.move(id, "up".equalsIgnoreCase(direction));
        return "redirect:/admin";
    }

    /**
     * Autoguardado de un campo editable (decisión D): autenticado, protegido por
     * CSRF, con whitelist de claves y tope de longitud. Lo invoca un fetch con
     * debounce desde el tablero.
     */
    @PostMapping("/admin/content")
    @ResponseBody
    public ResponseEntity<Void> updateContent(@Valid @RequestBody ContentUpdate body) {
        if (!siteContentService.isEditableKey(body.key())) {
            return ResponseEntity.badRequest().build();
        }
        siteContentService.updateContent(body.key(), body.value());
        return ResponseEntity.noContent().build();
    }
}

