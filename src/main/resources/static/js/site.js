/*
 * Scrollytelling del sitio público (port a JS vanilla del prototipo).
 *
 * El "stage" es sticky y cinco espaciadores dan la longitud de scroll; al
 * desplazar se calcula la escena activa y se hace cross-fade entre escenas. Los
 * puntos navegan con scroll suave y las tarjetas reproducen el video al hacer
 * click (una sola vez).
 *
 * Ajustes para celular:
 *  - La altura del paso se mide del propio espaciador (hoy 140svh), no de la
 *    ventana, y se guarda en caché.
 *  - Solo se vuelve a medir cuando cambia el ANCHO de la ventana: en móvil, la
 *    barra de direcciones al ocultarse cambia el alto y provocaba saltos.
 *  - Se exige pasar el 50% del paso más un margen (histéresis) antes de cambiar
 *    de escena, para no saltarse escenas con el impulso del scroll.
 */
(function () {
    "use strict";

    var STEPS = 5;
    var HYSTERESIS = 0.12; // margen extra sobre el 50% para confirmar el cambio
    var active = -1; // fuerza el primer render

    var story = document.querySelector("[data-story]");
    if (!story) {
        return;
    }
    var scenes = Array.prototype.slice.call(document.querySelectorAll("[data-scene]"));
    var dots = Array.prototype.slice.call(document.querySelectorAll("[data-dot]"));
    var cards = Array.prototype.slice.call(document.querySelectorAll(".uje-thumb[data-src]"));

    function updateScenes() {
        scenes.forEach(function (scene) {
            var i = parseInt(scene.dataset.scene, 10);
            if (i === active) {
                scene.style.opacity = "1";
                scene.style.transform = "translateY(0)";
                scene.style.pointerEvents = "auto";
                scene.style.zIndex = "2";
            } else {
                scene.style.opacity = "0";
                scene.style.pointerEvents = "none";
                scene.style.transform = i > active ? "translateY(40px)" : "translateY(-24px)";
                scene.style.zIndex = "1";
            }
        });
        dots.forEach(function (dot) {
            var on = parseInt(dot.dataset.dot, 10) === active;
            dot.style.background = on ? "var(--c-cielo-fuerte)" : "rgba(91,107,120,0.32)";
            dot.style.height = on ? "26px" : "10px";
            dot.style.borderRadius = on ? "999px" : "50%";
        });
    }

    /** Alto real de un espaciador (140svh); si no hay, cae a la altura de ventana. */
    function measureStepHeight() {
        var step = document.querySelector("[data-step]");
        var h = step ? step.getBoundingClientRect().height : 0;
        return h > 0 ? h : window.innerHeight;
    }

    var stepHeight = measureStepHeight();
    var lastWidth = window.innerWidth;

    function recomputeActive() {
        var top = story.getBoundingClientRect().top + window.scrollY;
        // Posición continua dentro de la historia, en unidades de "paso".
        var progress = (window.scrollY - top) / stepHeight;
        var target = Math.min(Math.max(Math.round(progress), 0), STEPS - 1);

        if (target === active) {
            return;
        }
        // Histéresis: solo cambiar cuando nos alejamos lo suficiente de la escena
        // actual, así el impulso del scroll no dispara cambios en el límite.
        if (active >= 0 && Math.abs(progress - active) < 0.5 + HYSTERESIS) {
            return;
        }
        active = target;
        updateScenes();
    }

    function goToStep(i) {
        var el = document.querySelector('[data-step="' + i + '"]');
        if (el) {
            window.scrollTo({
                top: el.getBoundingClientRect().top + window.scrollY,
                behavior: "smooth"
            });
        }
    }

    function playCard(card) {
        if (card.getAttribute("data-playing")) {
            return; // ya reproduciéndose: inyectar el iframe una sola vez
        }
        var src = card.getAttribute("data-src");
        if (!src) {
            return;
        }
        card.setAttribute("data-playing", "1");
        var iframe = document.createElement("iframe");
        iframe.src = src;
        iframe.title = "Video";
        iframe.setAttribute("allow",
            "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture");
        iframe.setAttribute("allowfullscreen", "");
        card.appendChild(iframe);
    }

    dots.forEach(function (dot) {
        dot.addEventListener("click", function () {
            goToStep(parseInt(dot.dataset.dot, 10));
        });
    });

    /**
     * En pantallas chicas el reproductor incrustado queda demasiado pequeño para
     * ver el video con comodidad, así que se abre YouTube (o su app) en una
     * pestaña nueva. En escritorio se mantiene la reproducción dentro de la
     * tarjeta, donde sí hay espacio suficiente.
     */
    function prefiereReproductorExterno() {
        return window.matchMedia("(max-width: 640px)").matches;
    }

    cards.forEach(function (card) {
        card.addEventListener("click", function () {
            var watchUrl = card.getAttribute("data-watch");
            if (watchUrl && prefiereReproductorExterno()) {
                window.open(watchUrl, "_blank", "noopener");
                return;
            }
            playCard(card);
        });
    });

    var ticking = false;
    function onScroll() {
        if (ticking) {
            return;
        }
        ticking = true;
        requestAnimationFrame(function () {
            ticking = false;
            recomputeActive();
        });
    }
    window.addEventListener("scroll", onScroll, { passive: true });

    // En celular, ocultar/mostrar la barra de direcciones dispara "resize" con un
    // alto distinto. Solo re-medimos cuando cambia el ancho (rotación o cambio de
    // ventana real); así el scroll no da saltos mientras se navega.
    window.addEventListener("resize", function () {
        if (window.innerWidth === lastWidth) {
            return;
        }
        lastWidth = window.innerWidth;
        stepHeight = measureStepHeight();
        onScroll();
    }, { passive: true });

    requestAnimationFrame(recomputeActive);
})();
