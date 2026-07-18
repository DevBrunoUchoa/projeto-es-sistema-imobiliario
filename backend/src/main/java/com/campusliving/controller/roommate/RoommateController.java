package com.campusliving.controller.roommate;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PerfilRoommateResponseDTO;
import com.campusliving.dto.roommate.RoommateCompativelDTO;
import com.campusliving.dto.roommate.RoommateMatchRequestDTO;
import com.campusliving.dto.roommate.RoommateMatchResponseDTO;
import com.campusliving.dto.roommate.RoommateMatchStatusUpdateDTO;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.usuario.User;
import com.campusliving.service.roommate.RoommateService;

import jakarta.validation.Valid;

/**
 * T5.8 — funcionalidade de roommates (RF-32 a RF-35).
 *
 * <p>O requerente é obtido do usuário autenticado no {@code SecurityContext}
 * (extraído do JWT via {@link AuthenticationPrincipal}), nunca de valor
 * enviado pelo cliente.</p>
 */
@RestController
@RequestMapping("/roommates")
public class RoommateController {

    private final RoommateService roommateService;

    public RoommateController(RoommateService roommateService) {
        this.roommateService = roommateService;
    }

    private static UUID idDe(User usuarioAutenticado) {
        return usuarioAutenticado == null ? null : usuarioAutenticado.getId();
    }

    // RF-32..35: roommates é uma funcionalidade de quem busca moradia. LOCADOR
    // puro não participa (não aluga para si mesmo), então bloqueamos o acesso a
    // toda a área — não basta esconder o link no frontend. O papel vem do
    // usuário autenticado (JWT), nunca de valor enviado pelo cliente.
    private static void exigirPapelDeRoommate(User usuarioAutenticado) {
        if (usuarioAutenticado != null && usuarioAutenticado.getTipoConta() == User.Tipo.LOCADOR) {
            throw new AcessoNegadoException();
        }
    }

    // --- T5.8.2: RF-32 -------------------------------------------------------
    @PostMapping("/perfil")
    public ResponseEntity<?> ativarPerfil(
            @RequestBody PerfilRoommateRequestDTO dto,
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        PerfilRoommateResponseDTO perfil = roommateService.ativarPerfil(dto, idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.OK).body(perfil);
    }

    @GetMapping("/perfil")
    public ResponseEntity<?> meuPerfil(
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        PerfilRoommateResponseDTO perfil = roommateService.buscarMeuPerfil(idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.OK).body(perfil);
    }

    @GetMapping("/match/pendentes")
    public ResponseEntity<?> listarPendentes(
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        List<RoommateMatchResponseDTO> pendentes = roommateService.listarSolicitacoesPendentes(idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.OK).body(pendentes);
    }

    // --- T5.8.3: RF-33 -------------------------------------------------------
    @GetMapping("/compativeis")
    public ResponseEntity<?> listarCompativeis(
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        List<RoommateCompativelDTO> compativeis = roommateService.listarCompativeis(idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.OK).body(compativeis);
    }

    // --- T5.8.4: RF-34 -------------------------------------------------------
    @PostMapping("/match")
    public ResponseEntity<?> solicitarMatch(
            @RequestBody @Valid RoommateMatchRequestDTO dto,
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        RoommateMatchResponseDTO match = roommateService.solicitarMatch(dto, idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    // --- T5.8.5: RF-34 / RF-35 -------------------------------------------------
    @PatchMapping("/match/{id}")
    public ResponseEntity<?> responderMatch(
            @PathVariable UUID id,
            @RequestBody @Valid RoommateMatchStatusUpdateDTO dto,
            @AuthenticationPrincipal User usuarioAutenticado) {
        exigirPapelDeRoommate(usuarioAutenticado);
        RoommateMatchResponseDTO match = roommateService.responderMatch(id, dto, idDe(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.OK).body(match);
    }
}