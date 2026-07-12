package com.campusliving.controller.avaliacao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.avaliacao.AvaliacaoRequestDTO;
import com.campusliving.dto.avaliacao.AvaliacaoResponseDTO;
import com.campusliving.dto.avaliacao.RespostaLocadorRequestDTO;
import com.campusliving.model.usuario.User;
import com.campusliving.service.avaliacao.AvaliacaoService;

import jakarta.validation.Valid;

// --- T5.7: RF-29 / RF-30 / RF-31 --------------------------------------------
// O requerente (avaliador/locador) vem do usuário autenticado no
// SecurityContext (JWT), nunca de valor enviado pelo cliente. RF-30 (cálculo
// de média) não tem endpoint próprio de escrita de propósito — é automático
// via trigger (V19); o valor já sai pronto tanto no perfil público do usuário
// (UserPublicProfileDTO) quanto embutido em cada item das listagens abaixo.
@RestController
@RequestMapping("/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<?> publicar(
            @RequestBody @Valid AvaliacaoRequestDTO dto,
            @AuthenticationPrincipal User usuarioAutenticado) {
        UUID avaliadorId = usuarioAutenticado == null ? null : usuarioAutenticado.getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(avaliacaoService.publicar(dto, avaliadorId));
    }

    // PUT (não POST): publicar e editar a resposta são a mesma operação
    // idempotente de substituição (ver RespostaLocadorRequestDTO/RF-31).
    @PutMapping("/{id}/resposta")
    public ResponseEntity<?> responder(
            @PathVariable("id") UUID avaliacaoId,
            @RequestBody @Valid RespostaLocadorRequestDTO dto,
            @AuthenticationPrincipal User usuarioAutenticado) {
        UUID locadorId = usuarioAutenticado == null ? null : usuarioAutenticado.getId();
        return ResponseEntity
                .ok(avaliacaoService.responder(avaliacaoId, dto, locadorId));
    }

    // Avaliações recebidas por um anúncio específico (tela de detalhe, RF-15).
    @GetMapping("/anuncio/{adId}")
    public ResponseEntity<Page<AvaliacaoResponseDTO>> listarPorAnuncio(
            @PathVariable UUID adId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarPorAnuncio(adId, pageable));
    }

    // Avaliações recebidas por um locador, somando todos os anúncios dele
    // (tela de perfil público, RF-07).
    @GetMapping("/locador/{locadorId}")
    public ResponseEntity<Page<AvaliacaoResponseDTO>> listarPorLocador(
            @PathVariable UUID locadorId,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(avaliacaoService.listarPorLocador(locadorId, pageable));
    }
}
