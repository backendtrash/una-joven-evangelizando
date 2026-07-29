package com.ujeva.controller;

import com.ujeva.model.CachedPost;
import com.ujeva.repository.CachedPostRepository;
import com.ujeva.service.SiteContentService;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

    public AdminController(
            SiteContentService siteContentService,
            CachedPostRepository cachedPostRepository) {
        this.siteContentService = siteContentService;
        this.cachedPostRepository = cachedPostRepository;
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
        return "admin/dashboard";
    }
}
