package com.campusliving.controller.interacao;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.interacao.ContatoResponseDTO;
import com.campusliving.dto.interacao.InteresseRequestDTO;
import com.campusliving.model.usuario.User;
import com.campusliving.service.interacao.ContatoService;

import jakarta.validation.Valid;

// --- T5.4.5: RF-28 / RNF-LEG-03 ---------------------------------------------
// Fica fora de /usuarios de propósito: "interesses" é uma rota de produto
// (POST /interesses), não uma sub-rota de um usuário específico — quem
// registra o interesse é sempre "eu mesmo" (o requerente autenticado, extraído
// do JWT via SecurityContext), nunca um id de terceiro na URL.
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
            @AuthenticationPrincipal User usuarioAutenticado) {
        UUID estudanteId = usuarioAutenticado == null ? null : usuarioAutenticado.getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contatoService.registrarInteresse(dto, estudanteId));
    }

    // "Minhas mensagens" — interesses que o estudante autenticado registrou.
    @GetMapping("/enviados")
    public ResponseEntity<List<ContatoResponseDTO>> listarEnviados(@AuthenticationPrincipal User usuarioAutenticado) {
        UUID estudanteId = usuarioAutenticado == null ? null : usuarioAutenticado.getId();
        return ResponseEntity.ok(contatoService.listarEnviados(estudanteId));
    }

    // Interesses recebidos em qualquer anúncio do locador autenticado.
    @GetMapping("/recebidos")
    public ResponseEntity<List<ContatoResponseDTO>> listarRecebidos(@AuthenticationPrincipal User usuarioAutenticado) {
        UUID locadorId = usuarioAutenticado == null ? null : usuarioAutenticado.getId();
        return ResponseEntity.ok(contatoService.listarRecebidos(locadorId));
    }
}
