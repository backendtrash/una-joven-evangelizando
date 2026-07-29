/*
 * Scrollytelling del sitio público (port a JS vanilla del prototipo).
 *
 * El "stage" es sticky y cinco espaciadores de alto 100dvh dan la longitud de
 * scroll; al desplazar se calcula la escena activa y se hace cross-fade entre
 * escenas. Los puntos navegan con scroll suave y las tarjetas reproducen el video
 * al hacer click (una sola vez). Se descarta el camino legacy `updateVideo`.
 */
(function () {
    "use strict";

    var STEPS = 5;
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

    function recomputeActive() {
        var vh = window.innerHeight;
        var top = story.getBoundingClientRect().top + window.scrollY;
        var idx = Math.round((window.scrollY - top) / vh);
        var clamped = Math.min(Math.max(idx, 0), STEPS - 1);
        if (clamped !== active) {
            active = clamped;
            updateScenes();
        }
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

    cards.forEach(function (card) {
        card.addEventListener("click", function () {
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
    window.addEventListener("resize", onScroll, { passive: true });

    requestAnimationFrame(recomputeActive);
})();
