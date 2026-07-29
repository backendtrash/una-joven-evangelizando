package com.ujeva.controller;

import com.ujeva.model.PostType;
import com.ujeva.service.CuratedVideoService;
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
 * <p>Sirve el login y el tablero de edición, el autoguardado de textos y el CRUD de
 * los videos curados: destacados (escena 1) y de oración (escena 4). Todo
 * {@code /admin/**} requiere autenticación.
 */
@Controller
public class AdminController {

    private final SiteContentService siteContentService;
    private final CuratedVideoService curatedVideoService;

    public AdminController(
            SiteContentService siteContentService,
            CuratedVideoService curatedVideoService) {
        this.siteContentService = siteContentService;
        this.curatedVideoService = curatedVideoService;
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
        model.addAttribute("featuredVideos", curatedVideoService.list(PostType.FEATURED));
        model.addAttribute("prayerVideos", curatedVideoService.list(PostType.PRAYER));
        return "admin/dashboard";
    }

    /**
     * Autoguardado de un campo de texto editable (decisión D): autenticado,
     * protegido por CSRF, con whitelist de claves y tope de longitud.
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

    // --- Videos destacados (escena 1): CRUD curado, autenticado y con CSRF. ---

    @PostMapping("/admin/featured/add")
    public String addFeatured(
            @RequestParam String title,
            @RequestParam(required = false) String meta,
            @RequestParam String url) {
        curatedVideoService.add(PostType.FEATURED, title, meta, url);
        return "redirect:/admin";
    }

    @PostMapping("/admin/featured/delete")
    public String deleteFeatured(@RequestParam Long id) {
        curatedVideoService.remove(PostType.FEATURED, id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/featured/move")
    public String moveFeatured(@RequestParam Long id, @RequestParam String direction) {
        curatedVideoService.move(PostType.FEATURED, id, "up".equalsIgnoreCase(direction));
        return "redirect:/admin";
    }

    // --- Videos de oración (escena 4): CRUD curado, autenticado y con CSRF. ---

    @PostMapping("/admin/prayer/add")
    public String addPrayer(
            @RequestParam String title,
            @RequestParam(required = false) String meta,
            @RequestParam String url) {
        curatedVideoService.add(PostType.PRAYER, title, meta, url);
        return "redirect:/admin";
    }

    @PostMapping("/admin/prayer/delete")
    public String deletePrayer(@RequestParam Long id) {
        curatedVideoService.remove(PostType.PRAYER, id);
        return "redirect:/admin";
    }

    @PostMapping("/admin/prayer/move")
    public String movePrayer(@RequestParam Long id, @RequestParam String direction) {
        curatedVideoService.move(PostType.PRAYER, id, "up".equalsIgnoreCase(direction));
        return "redirect:/admin";
    }
}
