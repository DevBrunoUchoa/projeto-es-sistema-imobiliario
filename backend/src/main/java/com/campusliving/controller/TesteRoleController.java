package com.campusliving.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
public class TesteRoleController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "✅ Acesso permitido para ADMIN!";
    }

    @GetMapping("/locador")
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public String locadorOnly() {
        return "✅ Acesso permitido para LOCADOR ou ADMIN!";
    }

    @GetMapping("/estudante")
    @PreAuthorize("hasAnyRole('ESTUDANTE', 'LOCADOR', 'MISTO', 'ADMIN')")
    public String estudanteOnly() {
        return "✅ Acesso permitido para qualquer usuário logado!";
    }

    @GetMapping("/publico")
    public String publico() {
        return "✅ Acesso permitido para qualquer um (público)!";
    }
}