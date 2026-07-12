package com.campusliving.controller.imovel;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.service.imovel.ImovelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/imoveis")
@RequiredArgsConstructor
public class ImovelController {

    private final ImovelService imovelService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public ResponseEntity<ImovelResponseDTO> criarImovel(
            @Valid @RequestBody ImovelRequestDTO request,
            Authentication authentication
    ) {
        // Extrair o email do usuário autenticado
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        // Passar o email para o service
        ImovelResponseDTO response = imovelService.criarImovel(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}