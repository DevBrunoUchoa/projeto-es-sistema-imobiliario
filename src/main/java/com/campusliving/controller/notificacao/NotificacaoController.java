package com.campusliving.controller.notificacao;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.notificacao.NotificacaoResponseDTO;
import com.campusliving.model.usuario.User;
import com.campusliving.service.notificacao.NotificacaoService;

/**
 * RF-39: notificações in-app do usuário autenticado (modelo de polling — o
 * frontend consulta periodicamente). Todas as rotas exigem autenticação e
 * operam apenas sobre as notificações do próprio usuário.
 */
@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService service;

    public NotificacaoController(NotificacaoService service) {
        this.service = service;
    }

    private static UUID idDe(User usuario) {
        return usuario == null ? null : usuario.getId();
    }

    @GetMapping
    public ResponseEntity<Page<NotificacaoResponseDTO>> listar(
            @AuthenticationPrincipal User usuario,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.listar(idDe(usuario), pageable));
    }

    @GetMapping("/nao-lidas")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(Map.of("naoLidas", service.contarNaoLidas(idDe(usuario))));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponseDTO> marcarComoLida(
            @PathVariable UUID id,
            @AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(service.marcarComoLida(id, idDe(usuario)));
    }

    @PatchMapping("/lidas")
    public ResponseEntity<Map<String, Integer>> marcarTodasComoLidas(@AuthenticationPrincipal User usuario) {
        return ResponseEntity.ok(Map.of("atualizadas", service.marcarTodasComoLidas(idDe(usuario))));
    }
}
