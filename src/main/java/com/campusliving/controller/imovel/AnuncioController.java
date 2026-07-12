package com.campusliving.controller.imovel;

import com.campusliving.dto.anuncio.AnuncioEstatisticasResponseDTO;
import com.campusliving.dto.anuncio.AnuncioMapaResponseDTO;
import com.campusliving.dto.anuncio.AnuncioPaginadoResponseDTO;
import com.campusliving.dto.anuncio.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.anuncio.AnuncioRequestDTO;
import com.campusliving.dto.anuncio.AnuncioResponseDTO;
import com.campusliving.dto.anuncio.AnuncioStatusUpdateDTO;
import com.campusliving.dto.anuncio.AnuncioUpdateRequestDTO;
import com.campusliving.service.imovel.AnuncioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/anuncios")
@RequiredArgsConstructor
public class AnuncioController {

    private final AnuncioService anuncioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public ResponseEntity<AnuncioResponseDTO> publicarAnuncio(
            @Valid @RequestBody AnuncioRequestDTO request,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        AnuncioResponseDTO response = anuncioService.publicarAnuncio(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public ResponseEntity<AnuncioResponseDTO> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AnuncioStatusUpdateDTO request,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        AnuncioResponseDTO response = anuncioService.atualizarStatus(id, request.getStatus(), email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public ResponseEntity<AnuncioResponseDTO> editarAnuncio(
            @PathVariable UUID id,
            @Valid @RequestBody AnuncioUpdateRequestDTO request,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        AnuncioResponseDTO response = anuncioService.editarAnuncio(id, request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnuncioDetalhesResponseDTO> buscarDetalhes(@PathVariable UUID id) {
        AnuncioDetalhesResponseDTO response = anuncioService.buscarDetalhes(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/estatisticas")
    @PreAuthorize("hasAnyRole('LOCADOR', 'ADMIN')")
    public ResponseEntity<AnuncioEstatisticasResponseDTO> buscarEstatisticas(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        AnuncioEstatisticasResponseDTO response = anuncioService.buscarEstatisticas(id, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/mapa")
    public ResponseEntity<List<AnuncioMapaResponseDTO>> buscarAnunciosParaMapa() {
        List<AnuncioMapaResponseDTO> response = anuncioService.buscarAnunciosParaMapa();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<AnuncioPaginadoResponseDTO> buscarAnuncios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Integer distanciaMaxMetros,
            @RequestParam(required = false) Boolean mobiliado,
            @RequestParam(required = false) Boolean permitePets,
            @RequestParam(required = false) Boolean permiteFumantes,
            @RequestParam(required = false) Boolean incluiAlimentacao,
            @RequestParam(required = false) String tipoOferta
    ) {
        AnuncioPaginadoResponseDTO response = anuncioService.buscarAnunciosComTexto(
                page, limit, sortBy, q,
                precoMax, distanciaMaxMetros,
                mobiliado, permitePets, permiteFumantes, incluiAlimentacao,
                tipoOferta
        );
        return ResponseEntity.ok(response);
    }
}