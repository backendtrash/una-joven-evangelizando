# Decisiones de arquitectura (ADR)

El sitio final es **visualmente 1:1** con el prototipo de diseño (layout, espaciado,
tipografía, color, componentes, estados y responsividad), pero su **comportamiento
es la versión de producción** y difiere a propósito de los atajos de demostración del
prototipo. Estas decisiones están **fijadas**: si una tarea parece requerir cambiar
una, hay que detenerse y señalarlo, no construir alrededor.

## ADR-A — Sin agregador filtrable separado
La grilla de la escena 1 se alimenta de la **YouTube Data API (cacheada)**.
Instagram y TikTok aparecen **solo** como enlaces de iconos sociales. El requisito
"contenido reciente de YouTube vía API, cacheado" se cumple; la subcláusula de
filtro por plataforma / embeds de IG-TikTok queda intencionalmente fuera, por diseño.

## ADR-B — Miniaturas reales de YouTube
Cada tile de video usa la **miniatura real de YouTube** bajo el overlay de gradiente
del diseño, con el botón de play blanco encima.

## ADR-C — Persistencia en PostgreSQL/JPA
La persistencia pasa del `localStorage` del prototipo a **PostgreSQL vía JPA**. El
sitio público se renderiza desde la BD; las ediciones aparecen en la siguiente carga.

## ADR-D — Autoguardado del panel por endpoint autenticado
El panel guarda vía un único `POST /admin/content` **autenticado y protegido con
CSRF** (claves en whitelist), invocado por `fetch` con *debounce* — no hay botón
"Guardar". Al ser solo para administración, no viola la regla de "sin REST para el
sitio público".

## ADR-E — Autenticación con Spring Security + BCrypt
Autenticación real con **Spring Security + BCrypt** (RA-01/RA-02). La página de login
**no muestra ninguna pista de credenciales**. Las credenciales de demo viven como
*placeholder* en el `README` / `docs/DEMO.md` (archivos del repo, nunca servidos);
los valores reales solo en variables de entorno. **Una sola aplicación** (un proyecto
Maven, un despliegue en Railway): el panel se aísla por autenticación, no por un
segundo despliegue.

## ADR-F — "Acerca de mí" editable · ⚠️ Reemplazada
**Decisión original:** la sección pública "Acerca de mí" renderizaba el campo editable
`aboutText` (párrafos separados por línea en blanco), con la cita del lema por defecto.

**Estado actual:** la sección se retiró del sitio. El rótulo "Acerca de mí" pasó a
encabezar la **escena 2** (antes "Mi misión"), que muestra el texto editable
`heroPresentacion`; tras la historia con scroll, el **footer** cierra la página. La
clave `aboutText` dejó de formar parte del contenido editable y su campo se quitó del
panel. *(La fila existente en la base de datos no se elimina; simplemente ya no se lee.)*

## ADR-F4 — Video destacado como primera tarjeta fijada · ⚠️ Reemplazada
**Decisión original:** el video destacado se resolvía por precedencia
`ytId(featuredUrl)` > `featuredId` > más reciente, y se renderizaba como la primera
tarjeta fijada de la escena 1.

**Estado actual:** reemplazada por **ADR-I**. La escena 1 muestra una lista curada de
varios videos destacados, en el orden que define la administradora, en lugar de un
único destacado resuelto por precedencia. Las claves `featuredId` y `featuredUrl`
desaparecieron del contenido editable.

## ADR-G — Videos curados por la administradora
Los videos de las grillas los gestiona la administradora. `CachedPost` lleva un
discriminador `type` con tres valores:

| Tipo | Origen | Quién lo mantiene |
|---|---|---|
| `RECENT` | Obtenido de la API de YouTube | El scheduler (refresco diario, upsert y prune) |
| `FEATURED` | Curado desde el panel | La administradora — grilla "Videos destacados" (escena 1) |
| `PRAYER` | Curado desde el panel | La administradora — grilla "Aprendamos a rezar juntos" (escena 4) |

El scheduler **solo toca las filas `RECENT`**; nunca modifica ni elimina las curadas.
El orden de las listas curadas lo fija `sort_order`. Las miniaturas de los videos
curados se derivan del id de YouTube (`img.youtube.com/vi/<id>/hqdefault.jpg`), sin
consumir cuota adicional de la API.

## ADR-I — Las grillas se alimentan de listas curadas
Ambas grillas de video (destacados y oración) se llenan **exclusivamente con las listas
que administra la creadora**, no con el resultado automático de la API.

**Motivo:** la creadora necesita decidir qué videos representan mejor su contenido y en
qué orden aparecen, en lugar de mostrar sin filtro lo más reciente del canal. No hay
límite en la cantidad de videos por sección.

**Consecuencia:** la integración con YouTube (`YouTubeService` y `ContentCacheService`)
se conserva operativa y sigue cacheando las filas `RECENT`, pero hoy **no alimenta
ninguna grilla**. Queda disponible por si más adelante se decide reactivar el llenado
automático, sin tener que reconstruirla.

## ADR-H — Facebook como cuarto icono social
Se incluye **Facebook** como cuarto icono social (hero y footer), según el diseño.
Las URLs de perfil reales son configuración, provistas más adelante.

## Placeholders a proveer antes del lanzamiento (no bloqueantes)
- IDs reales de videos de YouTube (el prototipo usaba `ScMzIvxBSi4`).
- URLs reales de los perfiles sociales.
- Contraseña real de administración (variable de entorno).

El lema se renderiza con `&` por defecto (según diseño); la admin puede editarlo.
