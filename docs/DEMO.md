# Acceso de demostración

> Este archivo documenta cómo acceder al panel. **No contiene credenciales reales de
> producción**: en producción el usuario y la contraseña se definen por variables de
> entorno (`ADMIN_INITIAL_USERNAME` / `ADMIN_INITIAL_PASSWORD`) y nunca se versionan.

El panel está en **`/admin`** y requiere iniciar sesión.

## Vista previa local (perfil `local`, H2 en memoria)

Al correr con `./mvnw spring-boot:run -Plocal`, se siembra un usuario de demostración
definido en `application-local.yml`:

- **Usuario:** `maria`
- **Contraseña:** `fiat2025`

Estas credenciales son **solo para la vista previa local** (base de datos en memoria,
se reinicia cada vez). No se usan en producción.

## Entorno con Supabase (perfiles `dev` / `prod`)

El usuario admin se siembra la primera vez a partir de las variables de entorno:

- `ADMIN_INITIAL_USERNAME` — usuario a crear (por defecto `maria`).
- `ADMIN_INITIAL_PASSWORD` — contraseña inicial (se guarda **hasheada con BCrypt**).

El sembrado es **idempotente**: solo crea el usuario si no existe. Por eso, para
cambiar la contraseña de un usuario ya creado no basta con editar la variable; hay que
eliminar la fila en la tabla `admin_user` y reiniciar (o usar un flujo de cambio de
contraseña).

## Recomendaciones de seguridad

- Usa una contraseña fuerte para `ADMIN_INITIAL_PASSWORD` en producción.
- Nunca subas el archivo `.env` ni credenciales reales al repositorio.
- Rota la contraseña de la base de datos si sospechas que se expuso.
