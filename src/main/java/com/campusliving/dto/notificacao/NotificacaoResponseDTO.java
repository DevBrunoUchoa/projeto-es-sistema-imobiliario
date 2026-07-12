package com.campusliving.dto.notificacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.notificacao.Notificacao;

/**
 * Notificação in-app (RF-39) exibida no sino/lista do usuário.
 */
public record NotificacaoResponseDTO(
        UUID id,
        String tipo,
        String titulo,
        String mensagem,
        boolean lida,
        OffsetDateTime dataCriacao) {

    public static NotificacaoResponseDTO of(Notificacao n) {
        return new NotificacaoResponseDTO(
                n.getId(),
                n.getTipo() == null ? null : n.getTipo().name(),
                n.getTitulo(),
                n.getMensagem(),
                n.isLida(),
                n.getDataCriacao());
    }
}
