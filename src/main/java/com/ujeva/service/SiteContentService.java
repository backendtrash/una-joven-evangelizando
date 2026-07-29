package com.ujeva.service;

import com.ujeva.model.SiteText;
import com.ujeva.repository.SiteTextRepository;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de contenido editable del sitio: textos y división en párrafos del
 * "Acerca de mí". Los videos destacados y de oración se gestionan como listas
 * curadas (ver {@link CuratedVideoService}), no como claves de texto.
 */
@Service
public class SiteContentService {

    /** Las 3 claves de texto editables desde el panel. */
    public static final List<String> KEYS =
            List.of("heroLema", "heroPresentacion", "aboutText");

    /** Extrae el id de video de las formas comunes de URL de YouTube. */
    private static final Pattern YT_ID =
            Pattern.compile("(?:youtu\\.be/|v=|embed/|shorts/)([A-Za-z0-9_-]{6,})");

    private final SiteTextRepository siteTextRepository;

    public SiteContentService(SiteTextRepository siteTextRepository) {
        this.siteTextRepository = siteTextRepository;
    }

    /** Indica si la clave es una de las editables (whitelist, decisión D). */
    public boolean isEditableKey(String key) {
        return KEYS.contains(key);
    }

    /**
     * Actualiza (o crea) el valor de una clave editable. El llamador debe validar
     * antes que la clave esté en la whitelist ({@link #isEditableKey}).
     */
    @Transactional
    public void updateContent(String key, String value) {
        SiteText siteText = siteTextRepository.findByContentKey(key)
                .orElseGet(() -> new SiteText(key, ""));
        siteText.setContentValue(value == null ? "" : value);
        siteTextRepository.save(siteText);
    }

    /**
     * Carga las claves editables como mapa; las claves ausentes devuelven "".
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
}
