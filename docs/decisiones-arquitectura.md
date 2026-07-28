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

## ADR-F — "Acerca de mí" editable
La sección pública "Acerca de mí" renderiza el campo editable `aboutText` (párrafos
separados por línea en blanco). Su valor por defecto es la cita del lema.

## ADR-F4 — Video destacado como primera tarjeta fijada
El video destacado se resuelve como `ytId(featuredUrl)` > `featuredId` > más reciente,
y se renderiza como la **primera tarjeta fijada** de la escena 1, cumpliendo el
requisito de "video destacado" de forma visible y fiel al diseño.

## ADR-G — Videos de oración gestionados por la admin
Los videos de oración (escena 4) los gestiona la administradora. `CachedPost` lleva
un discriminador `type`: `RECENT` (auto-obtenidos, refrescados a diario) vs `PRAYER`
(curados por la admin, nunca tocados por el scheduler), más `sort_order`. Las
miniaturas PRAYER se derivan del id de YouTube (`img.youtube.com/vi/<id>/hqdefault.jpg`),
sin consumir cuota extra.

## ADR-H — Facebook como cuarto icono social
Se incluye **Facebook** como cuarto icono social (hero y footer), según el diseño.
Las URLs de perfil reales son configuración, provistas más adelante.

## Placeholders a proveer antes del lanzamiento (no bloqueantes)
- IDs reales de videos de YouTube (el prototipo usaba `ScMzIvxBSi4`).
- URLs reales de los perfiles sociales.
- Contraseña real de administración (variable de entorno).

El lema se renderiza con `&` por defecto (según diseño); la admin puede editarlo.
