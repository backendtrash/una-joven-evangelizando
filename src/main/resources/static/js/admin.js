/*
 * Autoguardado del panel de administración (decisión D).
 *
 * Cada campo con [data-field] guarda su valor con un fetch "debounced" al endpoint
 * autenticado POST /admin/content, enviando el token CSRF por cabecera. Muestra el
 * estado en [data-save-status]. No hay botón "Guardar".
 */
(function () {
    "use strict";

    function meta(name) {
        var el = document.querySelector('meta[name="' + name + '"]');
        return el ? el.getAttribute("content") : null;
    }

    var csrfToken = meta("_csrf");
    var csrfHeader = meta("_csrf_header");
    var statusEl = document.querySelector("[data-save-status]");
    var fields = Array.prototype.slice.call(document.querySelectorAll("[data-field]"));

    function setStatus(text, color) {
        if (statusEl) {
            statusEl.textContent = text;
            statusEl.style.color = color || "#3BA776";
        }
    }

    function save(key, value) {
        setStatus("Guardando…", "#5B6B78");
        var headers = { "Content-Type": "application/json" };
        if (csrfHeader && csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        fetch("/admin/content", {
            method: "POST",
            headers: headers,
            body: JSON.stringify({ key: key, value: value })
        }).then(function (res) {
            if (res.ok) {
                setStatus("Guardado ✓", "#3BA776");
            } else {
                setStatus("No se pudo guardar", "#C0453B");
            }
        }).catch(function () {
            setStatus("Sin conexión", "#C0453B");
        });
    }

    fields.forEach(function (field) {
        var key = field.getAttribute("data-field");
        var timer = null;
        var eventName = field.tagName === "SELECT" ? "change" : "input";
        field.addEventListener(eventName, function () {
            if (timer) {
                clearTimeout(timer);
            }
            timer = setTimeout(function () {
                save(key, field.value);
            }, 500);
        });
    });
})();
