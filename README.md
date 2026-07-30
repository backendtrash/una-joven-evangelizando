# Una Joven Evangelizando

Sitio web de **Una Joven Evangelizando**: una landing pública tipo *scrollytelling*
que presenta a la creadora y su misión, y un **panel de administración** protegido
para editar los textos y curar los videos destacados y de oración. Todo el contenido
de cara al usuario está en **español**.

**🌐 Demo en vivo:** <https://www.unajovenevangelizando.com/>

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Maven (con Maven Wrapper) |
| Web / MVC | Spring MVC `@Controller` + Thymeleaf (renderizado en servidor) |
| Persistencia | Spring Data JPA (Hibernate) |
| Base de datos | PostgreSQL (Supabase) |
| Seguridad | Spring Security + BCrypt |
| API externa | YouTube Data API v3 (opcional) |
| Hosting | Railway |
| CI | GitHub Actions |

Documentación técnica en [`/docs`](docs/): arquitectura, decisiones (ADR), seguridad
y despliegue.

## Requisitos

- **JDK 17** (Temurin recomendado). No necesitas instalar Maven: usa el wrapper
  incluido (`mvnw` / `mvnw.cmd`).

## Cómo ejecutar

### Opción A — Vista previa local sin base de datos (H2 en memoria)
La forma más rápida de ver el sitio. No requiere Supabase ni configuración.

```bash
./mvnw spring-boot:run -Plocal
```
Abre <http://localhost:8080>. Los datos viven en memoria y **se borran al reiniciar**.
Credenciales del panel para este modo: usuario `maria`, contraseña `fiat2025`
(solo para vista previa local; ver [docs/DEMO.md](docs/DEMO.md)).

### Opción B — Local contra Supabase (datos persistentes)
1. Copia `.env.example` a `.env` y complétalo con tus credenciales de Supabase
   (ver [Variables de entorno](#variables-de-entorno)). El archivo `.env` está en
   `.gitignore` y **nunca se sube**.
2. Ejecuta (perfil `dev`, lee `.env` automáticamente):
   ```bash
   ./mvnw spring-boot:run
   ```
   Hibernate crea las tablas la primera vez (`ddl-auto=update`) y el sembrado inicial
   inserta los textos por defecto y el usuario admin.

### Ejecutar las pruebas
```bash
./mvnw -B test
```
Usan una base H2 en memoria; no requieren secretos ni red.

## Variables de entorno

Se leen de un archivo `.env` en local (perfil `dev`) o de las variables del servicio
en Railway (perfil `prod`). **Nunca** se ponen credenciales en el código.

| Variable | Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `local` (H2) · `dev` (Supabase, esquema evolutivo) · `prod` (Supabase, esquema validado) |
| `SUPABASE_DB_URL` | JDBC, p. ej. `jdbc:postgresql://<host>:5432/postgres?sslmode=require` |
| `SUPABASE_DB_USER` | usuario de Postgres (p. ej. `postgres.<ref>` con el session pooler) |
| `SUPABASE_DB_PASSWORD` | contraseña de la base de datos |
| `YOUTUBE_API_KEY` | llave de YouTube Data API v3 (opcional; sin ella, las grillas se llenan solo con videos curados) |
| `YOUTUBE_CHANNEL_ID` | id del canal (por defecto el del proyecto) |
| `ADMIN_INITIAL_USERNAME` | usuario admin a sembrar (por defecto `maria`) |
| `ADMIN_INITIAL_PASSWORD` | contraseña admin inicial (se guarda hasheada; si está vacía, no se siembra admin) |
| `SOCIAL_YOUTUBE` / `SOCIAL_INSTAGRAM` / `SOCIAL_TIKTOK` / `SOCIAL_FACEBOOK` | URLs de perfiles sociales |
| `PORT` | puerto del servidor (Railway lo inyecta; local usa 8080) |

## Funcionalidades

- **Landing scrollytelling**: hero, videos destacados, misión, formatos, videos de
  oración, "Acerca de mí" y footer — fiel al diseño.
- **Panel de administración** (`/admin`, requiere login): edición de textos con
  **autoguardado**, y **CRUD** de videos destacados y de oración (agregar, reordenar,
  eliminar; ilimitados).
- **Agregación de YouTube** (opcional): servicio que trae y cachea los videos recientes
  del canal, refrescado a diario.

## Despliegue

Ver la guía completa en [docs/despliegue.md](docs/despliegue.md) (Railway + Supabase,
`ddl-auto=validate`, HTTPS, checklist de backups).

## Flujo de trabajo (git)

- `master`: código estable/liberado. · `develop`: integración.
- Una rama `feature/T-XX-...` por tarea → Pull Request hacia `develop`.
- La CI (GitHub Actions) corre `./mvnw -B test` en cada push y PR; debe quedar verde.
