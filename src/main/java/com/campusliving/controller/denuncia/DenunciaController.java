package com.campusliving.controller.denuncia;

import com.campusliving.dto.denuncia.DenunciaRequestDTO;
import com.campusliving.dto.denuncia.DenunciaResponseDTO;
import com.campusliving.service.denuncia.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DenunciaResponseDTO> criarDenuncia(
            @Valid @RequestBody DenunciaRequestDTO request,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        DenunciaResponseDTO response = denunciaService.criarDenuncia(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/contar/{alvoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> contarDenuncias(@PathVariable UUID alvoId) {
        long total = denunciaService.contarDenuncias(alvoId);
        return ResponseEntity.ok(total);
    }
}