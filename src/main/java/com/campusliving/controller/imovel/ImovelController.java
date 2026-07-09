package com.campusliving.controller.imovel;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.service.imovel.ImovelService;

import jakarta.validation.Valid;

/**
 * T5.5 — gerenciamento de imóveis (RF-11). Mesmo esquema provisório de
 * autenticação via header {@code X-User-Id} do restante do projeto (ver
 * comentário detalhado em {@code UserController}, até o T5.3 existir).
 */
@RestController
@RequestMapping("/imoveis")
public class ImovelController {

    private final ImovelService imovelService;

    public ImovelController(ImovelService imovelService) {
        this.imovelService = imovelService;
    }

    // --- T5.5.1: RF-11 -------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody @Valid ImovelRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        ImovelResponseDTO imovel = imovelService.criar(dto, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(imovel);
    }
}
