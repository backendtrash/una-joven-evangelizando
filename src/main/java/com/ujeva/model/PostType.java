package com.ujeva.model;

/**
 * Discrimina el origen y el ciclo de vida de un {@link CachedPost}.
 *
 * <ul>
 *   <li>{@link #RECENT} — obtenido automáticamente de YouTube y refrescado a diario
 *       por el scheduler (upsert/prune).</li>
 *   <li>{@link #PRAYER} — curado por la administradora (videos de oración de la
 *       escena 4); el scheduler nunca lo toca.</li>
 * </ul>
 */
public enum PostType {
    RECENT,
    PRAYER
}
