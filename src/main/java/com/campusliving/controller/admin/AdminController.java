package com.campusliving.controller.admin;

import com.campusliving.dto.admin.AdminDenunciaResponseDTO;
import com.campusliving.dto.admin.AdminRelatorioResponseDTO;
import com.campusliving.dto.admin.AdminUsuarioResponseDTO;
import com.campusliving.dto.admin.AdminVerificarLocadorRequestDTO;
import com.campusliving.dto.admin.AdminVerificarLocadorResponseDTO;
import com.campusliving.service.admin.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminUsuarioResponseDTO>> listarUsuarios() {
        List<AdminUsuarioResponseDTO> response = adminService.listarUsuarios();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/denuncias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminDenunciaResponseDTO>> listarDenuncias() {
        List<AdminDenunciaResponseDTO> response = adminService.listarDenuncias();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/locadores/{id}/verificar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminVerificarLocadorResponseDTO> verificarLocador(
            @PathVariable UUID id,
            @Valid @RequestBody AdminVerificarLocadorRequestDTO request
    ) {
        AdminVerificarLocadorResponseDTO response = adminService.verificarLocador(id, request.getVerificado());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/relatorios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminRelatorioResponseDTO> gerarRelatorio() {
        AdminRelatorioResponseDTO response = adminService.gerarRelatorio();
        return ResponseEntity.ok(response);
    }
}