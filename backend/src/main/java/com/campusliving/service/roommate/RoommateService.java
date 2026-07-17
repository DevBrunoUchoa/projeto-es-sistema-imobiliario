package com.campusliving.service.roommate;

import java.util.List;
import java.util.UUID;

import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PerfilRoommateResponseDTO;
import com.campusliving.dto.roommate.PreferenciasRoommateRequestDTO;
import com.campusliving.dto.roommate.RoommateCompativelDTO;
import com.campusliving.dto.roommate.RoommateMatchRequestDTO;
import com.campusliving.dto.roommate.RoommateMatchResponseDTO;
import com.campusliving.dto.roommate.RoommateMatchStatusUpdateDTO;

public interface RoommateService {

    /** T5.8.1 — RF-10/RF-32: salva hábitos (upsert do perfil). */
    PerfilRoommateResponseDTO salvarPreferencias(UUID userId, PreferenciasRoommateRequestDTO dto, UUID requesterId);

    /** Perfil de roommate do próprio requerente, para pré-preencher a edição (default vazio se ainda não existe). */
    PerfilRoommateResponseDTO buscarMeuPerfil(UUID requesterId);

    /** Solicitações de match recebidas e ainda pendentes de resposta do requerente. */
    List<RoommateMatchResponseDTO> listarSolicitacoesPendentes(UUID requesterId);

    /** T5.8.2 — RF-32: ativa/atualiza o card público no mural. */
    PerfilRoommateResponseDTO ativarPerfil(PerfilRoommateRequestDTO dto, UUID requesterId);

    /** T5.8.3 — RF-33: perfis ativos/visíveis, ordenados por score de compatibilidade. */
    List<RoommateCompativelDTO> listarCompativeis(UUID requesterId);

    /** T5.8.4 — RF-34: cria solicitação PENDENTE, bloqueia duplicidade nos dois sentidos. */
    RoommateMatchResponseDTO solicitarMatch(RoommateMatchRequestDTO dto, UUID requesterId);

    /** T5.8.5 — RF-34/RF-35: aceita/recusa e notifica o solicitante. */
    RoommateMatchResponseDTO responderMatch(UUID matchId, RoommateMatchStatusUpdateDTO dto, UUID requesterId);
}
