# Revisión de seguridad (XSS / SQLi) — T-21

Resumen de la auditoría de seguridad de la aplicación frente a los requerimientos
RA-01…RI-04.

## XSS (Cross-Site Scripting) — RA-03

- **Todo dato controlado por el usuario se renderiza con `th:text`**, que auto-escapa
  el HTML en Thymeleaf. Campos auditados: `heroLema`, `heroPresentacion`, párrafos de
  `aboutText`, títulos y metadatos de los videos curados.
- **No se usa `th:utext`** (texto sin escapar) en ninguna plantilla.
- Prueba automatizada: [`XssEscapingTest`](../src/test/java/com/ujeva/controller/XssEscapingTest.java)
  guarda un payload `<script>…</script>` en `aboutText` y verifica que la portada lo
  muestra escapado (`&lt;script&gt;`), nunca como etiqueta ejecutable.

## Inyección SQL — RA-03

- **Solo consultas parametrizadas**: todo el acceso a datos pasa por Spring Data JPA
  (métodos derivados como `findByContentKey`, `findByVideoId`,
  `findByTypeOrderBySortOrderAsc`). No hay JPQL ni SQL nativo construido por
  concatenación de cadenas.

## Validación de entrada — RA-03

- Autoguardado: `POST /admin/content` valida el cuerpo con Bean Validation
  (`ContentUpdate`: `@NotBlank`, `@Size`) y además exige que la clave pertenezca a la
  **whitelist** de 3 claves editables; cualquier otra clave devuelve 400.
- Videos curados (destacados/oración): el título y la descripción se **recortan** a los
  topes de las columnas (200 / 120) en `CuratedVideoService`; el enlace se procesa con
  el regex `ytId` y se descarta si no contiene un id válido.

## Autenticación y sesión — RA-01 / RA-02

- Contraseñas **hasheadas con BCrypt** (`BCryptPasswordEncoder`); nunca en texto plano.
- `/admin/**` requiere autenticación; el acceso anónimo redirige al login. La página de
  login **no muestra credenciales** (decisión E).

## CSRF

- **Habilitado** (por defecto en Spring Security). Los formularios Thymeleaf incluyen el
  token automáticamente; el autoguardado por `fetch` lo envía por cabecera leyéndolo de
  un `<meta>`.

## Transporte y secretos — RI-04 / secretos

- En producción, TLS de Railway + `server.forward-headers-strategy=framework`.
- Conexión a la base de datos con `sslmode=require`.
- Sin secretos en el código: todo por variables de entorno; `.env` en `.gitignore`.
