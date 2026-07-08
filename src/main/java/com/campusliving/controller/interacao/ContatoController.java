package com.campusliving.controller.interacao;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.interacao.InteresseRequestDTO;
import com.campusliving.service.interacao.ContatoService;

import jakarta.validation.Valid;

// --- T5.4.5: RF-28 / RNF-LEG-03 ---------------------------------------------
// Fica fora de /usuarios de propósito: "interesses" é uma rota de produto
// (POST /interesses), não uma sub-rota de um usuário específico — quem
// registra o interesse é sempre "eu mesmo" (o requerente), nunca um id de
// terceiro na URL.
@RestController
@RequestMapping("/interesses")
public class ContatoController {

    private final ContatoService contatoService;

    public ContatoController(ContatoService contatoService) {
        this.contatoService = contatoService;
    }

    @PostMapping()
    public ResponseEntity<?> registrarInteresse(
            @RequestBody @Valid InteresseRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID estudanteId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contatoService.registrarInteresse(dto, estudanteId));
    }
}
