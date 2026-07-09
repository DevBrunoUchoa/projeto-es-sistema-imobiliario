package com.campusliving.controller.imovel;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.imovel.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.imovel.AnuncioRequestDTO;
import com.campusliving.dto.imovel.AnuncioResponseDTO;
import com.campusliving.dto.imovel.AnuncioStatusUpdateDTO;
import com.campusliving.dto.imovel.AnuncioUpdateRequestDTO;
import com.campusliving.dto.imovel.VisualizacaoPorDiaDTO;
import com.campusliving.service.imovel.AnuncioService;

import jakarta.validation.Valid;

/** T5.5 — gerenciamento de anúncios (RF-12 a RF-17). Mesmo esquema X-User-Id do restante do projeto. */
@RestController
@RequestMapping("/anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;

    public AnuncioController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    // --- T5.5.2: RF-12 -------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> publicar(
            @RequestBody @Valid AnuncioRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        AnuncioResponseDTO anuncio = anuncioService.publicar(dto, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(anuncio);
    }

    // --- T5.5.3: RF-13 -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable UUID id,
            @RequestBody AnuncioUpdateRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        AnuncioResponseDTO anuncio = anuncioService.atualizar(id, dto, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(anuncio);
    }

    // --- T5.5.4: RF-14 -------------------------------------------------------
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody @Valid AnuncioStatusUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        AnuncioResponseDTO anuncio = anuncioService.atualizarStatus(id, dto, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(anuncio);
    }

    // --- T5.5.5: RF-15 -------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetalhes(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        AnuncioDetalhesResponseDTO detalhes = anuncioService.getDetalhes(id, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(detalhes);
    }

    // --- T5.5.7: RF-17 -------------------------------------------------------
    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<?> getEstatisticas(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        List<VisualizacaoPorDiaDTO> estatisticas = anuncioService.getEstatisticas(id, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(estatisticas);
    }
}
