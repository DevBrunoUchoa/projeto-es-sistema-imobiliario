package com.campusliving.controller.roommate;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PerfilRoommateResponseDTO;
import com.campusliving.dto.roommate.RoommateCompativelDTO;
import com.campusliving.dto.roommate.RoommateMatchRequestDTO;
import com.campusliving.dto.roommate.RoommateMatchResponseDTO;
import com.campusliving.dto.roommate.RoommateMatchStatusUpdateDTO;
import com.campusliving.service.roommate.RoommateService;

import jakarta.validation.Valid;

/**
 * T5.8 — funcionalidade de roommates (RF-32 a RF-35).
 *
 * <p>Mesmo esquema de autenticação provisória do restante do T5 (header
 * {@code X-User-Id} até o T5.3 existir) — ver comentário detalhado em
 * {@code UserController}.</p>
 */
@RestController
@RequestMapping("/roommates")
public class RoommateController {

    private final RoommateService roommateService;

    public RoommateController(RoommateService roommateService) {
        this.roommateService = roommateService;
    }

    // --- T5.8.2: RF-32 -------------------------------------------------------
    @PostMapping("/perfil")
    public ResponseEntity<?> ativarPerfil(
            @RequestBody PerfilRoommateRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        PerfilRoommateResponseDTO perfil = roommateService.ativarPerfil(dto, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(perfil);
    }

    // --- T5.8.3: RF-33 -------------------------------------------------------
    @GetMapping("/compativeis")
    public ResponseEntity<?> listarCompativeis(
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        List<RoommateCompativelDTO> compativeis = roommateService.listarCompativeis(requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(compativeis);
    }

    // --- T5.8.4: RF-34 -------------------------------------------------------
    @PostMapping("/match")
    public ResponseEntity<?> solicitarMatch(
            @RequestBody @Valid RoommateMatchRequestDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        RoommateMatchResponseDTO match = roommateService.solicitarMatch(dto, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(match);
    }

    // --- T5.8.5: RF-34 / RF-35 -------------------------------------------------
    @PatchMapping("/match/{id}")
    public ResponseEntity<?> responderMatch(
            @PathVariable UUID id,
            @RequestBody @Valid RoommateMatchStatusUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) UUID requesterId) {
        RoommateMatchResponseDTO match = roommateService.responderMatch(id, dto, requesterId);
        return ResponseEntity.status(HttpStatus.OK).body(match);
    }
}
