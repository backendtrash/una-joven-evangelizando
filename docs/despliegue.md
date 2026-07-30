# Guía de despliegue (Railway + Supabase)

Producción: **Railway** construye el JAR desde el repo y lo ejecuta; **Supabase** es
la base de datos PostgreSQL. El tráfico va por **HTTPS** (TLS de Railway).

## 1. Base de datos — Supabase

1. Proyecto de Supabase creado.
2. Obtén la cadena de conexión (**Settings → Database → Connect → Session pooler**,
   puerto 5432, compatible con IPv4).
3. Arma los valores para las variables de entorno:
   - `SUPABASE_DB_URL=jdbc:postgresql://<host>:5432/postgres?sslmode=require`
   - `SUPABASE_DB_USER=postgres.<ref>`
   - `SUPABASE_DB_PASSWORD=<contraseña>`
4. **SSL obligatorio:** la URL debe incluir `?sslmode=require`.
5. **Tablas:** el esquema lo crea la app (Hibernate/ORM). En `dev` con
   `ddl-auto=update` se crean automáticamente. Para `prod` se usa
   `ddl-auto=validate` (no modifica el esquema, solo lo verifica), así que las tablas
   **deben existir antes** — se crean una vez arrancando en `dev` contra la misma base,
   o con un `schema.sql`.
6. **Backups:** en Supabase → Database → Backups, verifica que los backups automáticos
   estén activos (según tu plan) y anota la política de retención.
7. **RLS (opcional, recomendado):** activa Row Level Security en las 3 tablas **sin
   políticas** (deny-by-default). La app entra como rol `postgres` y **ignora RLS**, así
   que sigue funcionando; esto solo bloquea el API público de Supabase. Ver
   [seguridad.md](seguridad.md).

## 2. Aplicación — Railway

1. Crea un proyecto en Railway y conéctalo a este repositorio de GitHub.
2. El build usa el [`Dockerfile`](../Dockerfile) (multi-stage: compila el JAR con
   Maven + JDK 17 y lo ejecuta con JRE 17). En Railway → **Settings → Build →
   Builder**, elige **Dockerfile** (evita los problemas de `JAVA_HOME` de la
   autodetección de Nixpacks/Railpack).
3. Rama a desplegar: **`master`** (Settings → Source).
4. Configura las **variables de entorno del servicio** (Settings → Variables):
   ```
   SPRING_PROFILES_ACTIVE=prod
   SUPABASE_DB_URL=...
   SUPABASE_DB_USER=...
   SUPABASE_DB_PASSWORD=...
   ADMIN_INITIAL_USERNAME=...
   ADMIN_INITIAL_PASSWORD=...        # contraseña fuerte
   YOUTUBE_API_KEY=...               # opcional
   SOCIAL_YOUTUBE=... SOCIAL_INSTAGRAM=... SOCIAL_TIKTOK=... SOCIAL_FACEBOOK=...
   ```
   > `PORT` lo inyecta Railway automáticamente; no lo definas tú.
5. Despliega. Railway asigna un dominio HTTPS; puedes conectar tu **dominio propio** en
   Settings → Domains.

## 3. Perfil de producción

El perfil `prod` ([application-prod.yml](../src/main/resources/application-prod.yml)) ya
deja listo:
- `spring.jpa.hibernate.ddl-auto=validate` — no toca el esquema.
- `server.forward-headers-strategy=framework` — respeta las cabeceras del proxy de
  Railway para saber que el tráfico entra por HTTPS (RI-04).
- Plantillas Thymeleaf cacheadas.

## 4. Verificación post-despliegue

- El sitio responde por **HTTPS** en el dominio de Railway.
- `/` carga la portada con el contenido desde Supabase.
- `/admin` pide login y entra con las credenciales de `ADMIN_INITIAL_*`.
- Los cambios en el panel persisten (se ven al recargar).

## Notas

- **Secretos:** solo por variables de entorno. `.env` está en `.gitignore` y no se
  despliega.
- **Migraciones:** para un manejo de esquema más estricto en producción se recomienda
  Flyway, pero es una dependencia fuera del stack fijo del proyecto; evaluar antes de
  agregarla.
