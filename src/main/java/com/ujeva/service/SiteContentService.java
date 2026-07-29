package com.ujeva.service;

import com.ujeva.model.CachedPost;
import com.ujeva.model.PostType;
import com.ujeva.model.SiteText;
import com.ujeva.repository.CachedPostRepository;
import com.ujeva.repository.SiteTextRepository;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Lógica de contenido del sitio: textos editables, división en párrafos del
 * "Acerca de mí" y resolución del video destacado.
 *
 * <p>Resolución del destacado (decisión F4): {@code ytId(featuredUrl)} tiene
 * prioridad sobre {@code featuredId}, y este sobre "el video RECENT más reciente".
 */
@Service
public class SiteContentService {

    /** Las 5 claves editables desde el panel. */
    public static final List<String> KEYS =
            List.of("heroLema", "heroPresentacion", "aboutText", "featuredId", "featuredUrl");

    /** Extrae el id de video de las formas comunes de URL de YouTube. */
    private static final Pattern YT_ID =
            Pattern.compile("(?:youtu\\.be/|v=|embed/|shorts/)([A-Za-z0-9_-]{6,})");

    private final SiteTextRepository siteTextRepository;
    private final CachedPostRepository cachedPostRepository;

    public SiteContentService(
            SiteTextRepository siteTextRepository,
            CachedPostRepository cachedPostRepository) {
        this.siteTextRepository = siteTextRepository;
        this.cachedPostRepository = cachedPostRepository;
    }

    /** Indica si la clave es una de las 5 editables (whitelist, decisión D). */
    public boolean isEditableKey(String key) {
        return KEYS.contains(key);
    }

    /**
     * Actualiza (o crea) el valor de una clave editable. El llamador debe validar
     * antes que la clave esté en la whitelist ({@link #isEditableKey}).
     */
    @org.springframework.transaction.annotation.Transactional
    public void updateContent(String key, String value) {
        SiteText siteText = siteTextRepository.findByContentKey(key)
                .orElseGet(() -> new SiteText(key, ""));
        siteText.setContentValue(value == null ? "" : value);
        siteTextRepository.save(siteText);
    }

    /**
     * Carga las 5 claves editables como mapa; las claves ausentes devuelven "".
     */
    public Map<String, String> loadContent() {
        Map<String, String> content = new LinkedHashMap<>();
        for (String key : KEYS) {
            content.put(key, siteTextRepository.findByContentKey(key)
                    .map(SiteText::getContentValue)
                    .orElse(""));
        }
        return content;
    }

    /**
     * Extrae el id de un video a partir de una URL de YouTube (youtu.be, v=,
     * embed/, shorts/). Devuelve vacío si la URL es nula o no contiene un id.
     */
    public Optional<String> ytId(String url) {
        if (url == null) {
            return Optional.empty();
        }
        Matcher matcher = YT_ID.matcher(url);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    /**
     * Divide el texto "Acerca de mí" en párrafos separados por líneas en blanco.
     * Ignora fragmentos vacíos; un texto nulo o en blanco devuelve lista vacía.
     */
    public List<String> aboutParagraphs(String aboutText) {
        if (aboutText == null || aboutText.isBlank()) {
            return List.of();
        }
        return Arrays.stream(aboutText.split("\\n\\s*\\n"))
                .map(String::trim)
                .filter(paragraph -> !paragraph.isEmpty())
                .toList();
    }

    /**
     * Resuelve el id del video destacado según la precedencia F4:
     * {@code ytId(featuredUrl)} &gt; {@code featuredId} &gt; RECENT más reciente.
     */
    public Optional<String> resolveFeaturedVideoId(String featuredUrl, String featuredId) {
        Optional<String> fromUrl = ytId(featuredUrl);
        if (fromUrl.isPresent()) {
            return fromUrl;
        }
        if (featuredId != null && !featuredId.isBlank()) {
            return Optional.of(featuredId);
        }
        return cachedPostRepository.findByTypeOrderByPublishedAtDesc(PostType.RECENT)
                .stream()
                .findFirst()
                .map(CachedPost::getVideoId);
    }
}
