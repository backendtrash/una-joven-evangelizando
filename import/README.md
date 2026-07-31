# Importación del tablero de proyecto

Este directorio contiene el artefacto para poblar el tablero de gestión del
proyecto. La herramienta de gestión que se usó es **Trello**. El tablero real se
crea en **Trello bajo la cuenta de la dueña del proyecto**; aquí solo se versiona
el archivo de importación, no una acción dentro de esa cuenta.

## Archivo

- **`board.csv`** — una fila por tarea del backlog (`T-01`…`T-24`) más las tarjetas
  *fuera de alcance*. Columnas:
  - `Name` — título de la tarjeta (incluye el ID de tarea).
  - `Labels` — etiqueta de categoría (`setup`, `backend`, `persistencia`,
    `integracion`, `seguridad`, `frontend`, `admin`, `docs`, `ci`,
    `fuera-de-alcance`).
  - `Milestone` — `Beta` o `GA` (vacío para fuera de alcance).
  - `Estimacion` — estimación de esfuerzo.
  - `Origen` — requerimiento de origen (rúbrica Act.2 o ID de requerimiento
    `RA-/RI-/F-` / decisión bloqueada).
  - `Descripcion` — **qué** hace la tarea y **por qué** (rúbrica Act.2 #1).

## Importación en Trello

Trello importa CSV de forma nativa:

1. En un tablero nuevo: **Add a list** → menú de la lista → **Import** (o el botón
   *Import CSV* del tablero, según el plan de Trello).
2. Sube `board.csv`.
3. Mapea las columnas: `Name` → *Card name*, `Descripcion` → *Description*,
   `Labels` → *Labels*. `Milestone`, `Estimacion` y `Origen` pueden ir como
   *custom fields* o quedarse dentro de la descripción.
4. Crea manualmente los milestones **Beta** y **GA** si tu plan de Trello no los
   deriva del campo.

## Milestones

- **Beta** — todo corriendo end-to-end local: BD, modelo, landing responsiva,
  integración YouTube, agregador y panel admin con seguridad.
- **GA** — producción: HTTPS en Railway, dominio propio, backups de Supabase
  verificados y documentación completa.
