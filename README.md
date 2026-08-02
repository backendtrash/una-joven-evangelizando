# Una Joven Evangelizando

Sitio web de **Una Joven Evangelizando**: una landing pública tipo *scrollytelling*
que presenta a la creadora y su misión, y un **panel de administración** protegido
para editar los textos y curar los videos destacados y de oración. Todo el contenido
de cara al usuario está en **español**.

**🌐 Demo en vivo:** <https://www.unajovenevangelizando.com/>

---

## Tabla de contenidos

- [Resumen ejecutivo](#resumen-ejecutivo)
  - [Descripción](#descripción)
  - [Problema identificado](#problema-identificado)
  - [Solución](#solución)
  - [Arquitectura](#arquitectura)
- [Requerimientos](#requerimientos)
- [Instalación](#instalación)
  - [Ambiente de desarrollo](#ambiente-de-desarrollo)
  - [Ejecutar las pruebas](#ejecutar-las-pruebas)
  - [Despliegue a producción](#despliegue-a-producción)
- [Configuración](#configuración)
  - [Perfiles y archivos de configuración](#perfiles-y-archivos-de-configuración)
  - [Variables de entorno](#variables-de-entorno)
- [Uso](#uso)
  - [Guía para el usuario final](#guía-para-el-usuario-final)
  - [Guía para el usuario administrador](#guía-para-el-usuario-administrador)
- [Contribución](#contribución)
- [Roadmap](#roadmap)
- [Documentación adicional](#documentación-adicional)

Documentación técnica ampliada en [`/docs`](docs/):
[arquitectura](docs/arquitectura.md) ·
[decisiones (ADR)](docs/decisiones-arquitectura.md) ·
[seguridad](docs/seguridad.md) ·
[despliegue](docs/despliegue.md) ·
[demo](docs/DEMO.md).

---

## Resumen ejecutivo

### Descripción

*Una Joven Evangelizando* es el canal de una creadora de contenido católico que se
dedica a la evangelización digital. Publica reflexiones sobre la fe, explica temas de
doctrina en un lenguaje cercano y acompaña a su comunidad en las fechas del calendario
litúrgico. Opera de forma unipersonal: ella misma produce, edita, publica y atiende a
su audiencia.

Este proyecto es su **sitio web oficial**: el lugar central y propio donde vive su
contenido, bajo su propio dominio.

### Problema identificado

Su presencia digital estaba **dispersa en varias plataformas** —YouTube, Instagram,
TikTok y Facebook— sin ningún lugar que la reuniera. Cada red funcionaba como una isla:

- Quien la descubría en una plataforma no tenía una ruta clara hacia las demás, así que
  se perdía audiencia entre redes.
- Toda su presencia vivía en plataformas de terceros, sujeta a sus algoritmos, sus
  formatos y sus cambios de reglas. No tenía un espacio propio bajo su control.
- No existía un lugar ordenado donde presentar quién es, cuál es su misión y qué tipo
  de contenido ofrece.

### Solución

Un sitio web público que funciona como la **casa digital del canal**:

- Una **landing scrollytelling** que presenta a la creadora, su misión y su contenido
  de forma narrativa, con una dirección propia y permanente.
- Un **agregador de contenido** que trae automáticamente los videos recientes de
  YouTube mediante su API oficial, con caché diario para no depender de la
  disponibilidad del servicio externo.
- Enlaces a sus perfiles de Instagram, TikTok y Facebook, para que la comunidad la
  encuentre en todas partes desde un solo punto.
- Un **panel de administración** que le permite editar los textos y curar los videos
  destacados y de oración por su cuenta, sin tocar código ni depender de nadie.

### Arquitectura

Aplicación web monolítica con **patrón MVC renderizado en el servidor**:

```
Navegador (visitante / administradora)
        │ HTTPS
        ▼
Spring Boot en Railway (Tomcat embebido, Java 17)
   Controlador (@Controller) ──► Vista (plantillas Thymeleaf)
        │
        ▼
   Modelo (entidades JPA + repositorios)
        │ JDBC/SSL                  │ REST + API key
        ▼                           ▼
PostgreSQL (Supabase)        YouTube Data API v3
```

El detalle completo, incluyendo los componentes de la infraestructura de desarrollo
(GitHub, GitHub Actions, tablero de proyecto), está en
[docs/arquitectura.md](docs/arquitectura.md); las decisiones técnicas y su
justificación, en [docs/decisiones-arquitectura.md](docs/decisiones-arquitectura.md).

---

## Requerimientos

### Servidores y servicios

| Componente | Tecnología | Notas |
|---|---|---|
| Servidor de aplicación | **Tomcat embebido** (incluido en Spring Boot) | No requiere instalar un servidor externo ni desplegar un WAR |
| Servidor web / TLS | **Railway** | Termina HTTPS y enruta al contenedor de la aplicación |
| Base de datos | **PostgreSQL 15+** gestionado en **Supabase** | Conexión cifrada (`sslmode=require`); respaldos gestionados por la plataforma |
| API externa | **YouTube Data API v3** | Opcional: sin llave, las grillas se llenan solo con videos curados |

### Versiones y herramientas

- **JDK 17** (Temurin recomendado) — versión LTS requerida para compilar y ejecutar.
- **Maven** — no necesitas instalarlo: el repositorio incluye el **Maven Wrapper**
  (`mvnw` / `mvnw.cmd`).
- **Git** — para clonar el repositorio y trabajar con ramas.
- Navegador moderno (Chrome, Firefox, Edge o Safari en versiones vigentes).

### Paquetes y dependencias principales

Se gestionan con Maven y se declaran en [`pom.xml`](pom.xml):

| Dependencia | Para qué se usa |
|---|---|
| `spring-boot-starter-web` | MVC, servidor embebido y manejo de peticiones HTTP |
| `spring-boot-starter-thymeleaf` | Motor de plantillas para el renderizado en servidor |
| `spring-boot-starter-data-jpa` | Persistencia con Hibernate y repositorios |
| `spring-boot-starter-security` | Autenticación del panel y hash BCrypt de contraseñas |
| `spring-boot-starter-validation` | Validación de los datos que entran al panel |
| `postgresql` | Driver JDBC de PostgreSQL (runtime) |
| `h2` | Base de datos en memoria para pruebas y vista previa local (test/local) |
| `spring-boot-starter-test` | JUnit 5, AssertJ y Mockito |
| `spring-security-test` | Pruebas de las reglas de acceso |

---

## Instalación

### Ambiente de desarrollo

Clona el repositorio y colócate en él:

```bash
git clone https://github.com/backendtrash/una-joven-evangelizando.git
cd una-joven-evangelizando
```

Después elige una de las dos formas de ejecutarlo.

#### Opción A — Vista previa local sin base de datos (H2 en memoria)

La forma más rápida de ver el sitio. No requiere Supabase ni configuración.

```bash
./mvnw spring-boot:run -Plocal
```

Abre <http://localhost:8080>. Los datos viven en memoria y **se borran al reiniciar**.
Credenciales del panel para este modo: usuario `maria`, contraseña `fiat2025`
(solo para vista previa local; ver [docs/DEMO.md](docs/DEMO.md)).

#### Opción B — Local contra Supabase (datos persistentes)

1. Copia `.env.example` a `.env` y complétalo con tus credenciales de Supabase
   (ver [Variables de entorno](#variables-de-entorno)). El archivo `.env` está en
   `.gitignore` y **nunca se sube al repositorio**.
2. Ejecuta con el perfil `dev`, que lee `.env` automáticamente:

   ```bash
   ./mvnw spring-boot:run
   ```

   Hibernate crea las tablas la primera vez (`ddl-auto=update`) y el sembrado inicial
   inserta los textos por defecto y el usuario administrador.

### Ejecutar las pruebas

De forma manual, desde la raíz del proyecto:

```bash
./mvnw -B test
```

La suite corre sobre una base **H2 en memoria**: no requiere secretos, credenciales ni
acceso a la red. Cubre utilidades, repositorios, servicios, controladores, reglas de
seguridad y sembrado de datos.

Para ejecutar una sola clase de prueba:

```bash
./mvnw -B test -Dtest=ValidadorTest
```

Las mismas pruebas se ejecutan automáticamente en **GitHub Actions** en cada push y
cada pull request; el pipeline debe quedar en verde antes de integrar cambios.

### Despliegue a producción

El sitio está desplegado en **Railway**, que construye la imagen a partir del
repositorio y ejecuta el artefacto con el perfil `prod`. En resumen:

1. Conecta el repositorio de GitHub al servicio en Railway.
2. Carga las variables de entorno del servicio (ver la tabla más abajo), con
   `SPRING_PROFILES_ACTIVE=prod`.
3. Railway compila con el Maven Wrapper, genera el JAR ejecutable y lo levanta.
4. Configura el dominio propio; Railway emite y renueva el certificado TLS.

Para generar el artefacto localmente:

```bash
./mvnw -B clean package
java -jar target/*.jar
```

La guía completa —incluyendo `ddl-auto=validate`, HTTPS y el checklist de respaldos—
está en [docs/despliegue.md](docs/despliegue.md).

---

## Configuración

### Perfiles y archivos de configuración

La configuración vive en `src/main/resources` y se activa por perfil:

| Perfil | Base de datos | Esquema | Cuándo se usa |
|---|---|---|---|
| `local` | H2 en memoria | se crea al arrancar | Vista previa rápida sin credenciales |
| `dev` | Supabase (PostgreSQL) | `ddl-auto=update` | Desarrollo local con datos persistentes |
| `prod` | Supabase (PostgreSQL) | `ddl-auto=validate` | Producción en Railway |

El perfil se selecciona con la variable `SPRING_PROFILES_ACTIVE`, o con el perfil de
Maven `-Plocal` en el caso de la vista previa.

Archivos relevantes:

- `application.yml` — configuración base común a todos los perfiles.
- Configuración por perfil — ajustes de origen de datos, esquema y logging.
- `.env.example` — plantilla de las variables necesarias; se copia a `.env` en local.
- `Dockerfile` — estandariza la construcción del contenedor de despliegue.

### Variables de entorno

Se leen de un archivo `.env` en local (perfil `dev`) o de las variables del servicio
en Railway (perfil `prod`). **Nunca** se ponen credenciales en el código ni se suben
al repositorio.

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

---

## Uso

### Guía para el usuario final

El usuario final es cualquier persona de la comunidad que visita el sitio. No necesita
cuenta ni registro: todo el contenido público es de acceso libre.

Al entrar a <https://www.unajovenevangelizando.com/> encuentra una página única que se
recorre desplazándose hacia abajo, con estas secciones en orden:

1. **Portada (hero)** — el nombre del canal y el lema que identifica la misión de la
   creadora.
2. **Videos destacados** — la selección de videos que la creadora curó como más
   representativos. En celular, al tocar una tarjeta el video se abre en YouTube; en
   computadora se reproduce dentro de la misma tarjeta.
3. **Acerca de mí** — quién es y qué busca con su labor de evangelización.
4. **Formatos** — los tipos de contenido que publica.
5. **Videos de oración** — la selección de videos pensados para acompañar la oración.
6. **Pie de página** — enlaces a sus perfiles de YouTube, Instagram, TikTok y Facebook.

**Recomendaciones de uso:**

- El sitio está diseñado para leerse en el celular; funciona igual en tableta y
  computadora.
- Si una grilla de videos aparece vacía, significa que aún no hay contenido curado en
  esa sección; el resto del sitio sigue funcionando con normalidad.

### Guía para el usuario administrador

La administradora es la creadora del canal. El panel le permite mantener el sitio al día
sin tocar código.

**1. Entrar al panel**

Ve a `/admin` (por ejemplo, <https://www.unajovenevangelizando.com/admin>). Se pide
usuario y contraseña. Si no has iniciado sesión, cualquier intento de entrar al panel
te redirige a esta pantalla.

**2. Editar los textos del sitio**

Dentro del panel aparecen los textos editables de la landing (títulos, misión, "Acerca
de mí", etc.). Se editan directamente en su campo y **se guardan solos**: no hay que
buscar un botón de "Guardar", el cambio se envía al servidor poco después de dejar de
escribir. Al recargar el sitio público, el texto nuevo ya aparece.

**3. Administrar los videos destacados y de oración**

Cada una de las dos secciones de video tiene su propia lista, y en ambas puedes:

- **Agregar** un video nuevo (se identifica por su enlace o id de YouTube).
- **Reordenar** los videos para decidir cuál aparece primero.
- **Eliminar** los que ya no quieras mostrar.

No hay límite en la cantidad de videos por sección.

**4. Cerrar sesión**

Cierra la sesión al terminar, sobre todo si usas una computadora compartida. El panel
solo es accesible con sesión iniciada.

**Buenas prácticas de seguridad:**

- Cambia la contraseña inicial sembrada por el sistema en cuanto tomes posesión del
  panel.
- No compartas las credenciales por mensajes ni las anotes en lugares visibles.
- Las contraseñas se almacenan cifradas con BCrypt; nadie —ni el desarrollador— puede
  leerlas en texto plano.

---

## Contribución

Este proyecto se desarrolla con dos ramas principales: **`master`** (código estable y
liberado) y **`develop`** (integración del trabajo en curso). Ninguna de las dos se
modifica directamente: todo cambio entra por **pull request**.

Pasos para contribuir:

**1. Clona el repositorio**

```bash
git clone https://github.com/backendtrash/una-joven-evangelizando.git
cd una-joven-evangelizando
```

**2. Colócate en `develop` y actualízala**

```bash
git checkout develop
git pull origin develop
```

**3. Crea una rama única para tu tarea**

Usa el prefijo `feature/` y el identificador de la tarea:

```bash
git checkout -b feature/T-XX-descripcion-breve
```

**4. Trabaja y haz commits descriptivos**

Prefija el mensaje con el identificador de la tarea:

```bash
git add .
git commit -m "T-XX: descripcion de lo que se hizo"
```

**5. Verifica que las pruebas pasen antes de subir**

```bash
./mvnw -B test
```

**6. Sube tu rama**

```bash
git push -u origin feature/T-XX-descripcion-breve
```

**7. Abre un Pull Request hacia `develop`**

En GitHub aparecerá el aviso *Compare & pull request*. Asegúrate de que la rama destino
sea **`develop`** (no `master`). Describe qué cambia y por qué.

**8. Espera la revisión y el merge**

La integración continua ejecuta el build y las pruebas automáticamente: **el pull
request debe quedar en verde**. Una vez revisado y aprobado, se hace el merge a
`develop`. Cuando `develop` alcanza un estado estable, se integra a `master`.

---

## Roadmap

Funcionalidades que quedaron **fuera del alcance de la primera versión** y están
registradas para versiones futuras:

| # | Funcionalidad | Descripción |
|---|---|---|
| 1 | **Módulo de contacto e invitaciones** | Formulario para que parroquias, grupos y medios envíen invitaciones y propuestas, con registro de solicitudes y notificación por correo. |
| 2 | **Incrustación de Instagram y TikTok** | Mostrar las publicaciones de esas redes dentro del sitio, no solo enlazar a los perfiles. |
| 3 | **Filtro por plataforma** | Permitir al visitante filtrar el contenido agregado según su red de origen. Cobra sentido cuando haya más de una fuente. |
| 4 | **Analítica de visitas** | Métricas de tráfico y comportamiento para entender qué contenido funciona mejor. |
| 5 | **Eventos y calendario litúrgico** | Sección con las fechas del calendario litúrgico y los eventos en los que participa la creadora. |
| 6 | **Soporte multi-idioma** | Internacionalización de la interfaz, si la audiencia deja de ser únicamente hispanohablante. |
| 7 | **Frontend desacoplado** | Migración de la vista a una aplicación React independiente consumiendo una API REST, si el proyecto crece en complejidad. |

---

## Documentación adicional

| Documento | Contenido |
|---|---|
| [docs/arquitectura.md](docs/arquitectura.md) | Arquitectura detallada, capas y componentes |
| [docs/decisiones-arquitectura.md](docs/decisiones-arquitectura.md) | Registro de decisiones técnicas (ADR) y su justificación |
| [docs/seguridad.md](docs/seguridad.md) | Modelo de seguridad: autenticación, hashing, protección de rutas y validación de entradas |
| [docs/despliegue.md](docs/despliegue.md) | Guía de despliegue en Railway y Supabase, HTTPS y respaldos |
| [docs/DEMO.md](docs/DEMO.md) | Instrucciones para la vista previa local y credenciales de demostración |
