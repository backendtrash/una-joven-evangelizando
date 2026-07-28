package com.ujeva.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del panel de administración.
 *
 * <p>Por ahora sirve la página de login ({@code /admin/login}). El tablero y los
 * endpoints de edición se agregan en tareas posteriores (T-17..T-20). El acceso a
 * {@code /admin/**} está protegido por Spring Security.
 */
@Controller
public class AdminController {

    /** Página de inicio de sesión del panel (renderizada por Spring Security como loginPage). */
    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }
}
