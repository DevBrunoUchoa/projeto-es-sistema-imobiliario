package com.campusliving.model.roommate;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Mapeia {@code roommate_matches} (V8) — RF-34/RF-35. Entidade "stub" (ver PerfilRoommate). */
@Entity
@Table(name = "roommate_matches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateMatch {

    public enum Status {
        PENDENTE, ACEITO, RECUSADO, CANCELADO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("solicitanteId")
    @Column(name = "solicitante_id", nullable = false)
    private UUID solicitanteId;

    @JsonProperty("destinatarioId")
    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @JsonProperty("mensagemInicial")
    @Column(name = "mensagem_inicial")
    private String mensagemInicial;

    @JsonProperty("dataSolicitacao")
    @Column(name = "data_solicitacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataSolicitacao;

    @JsonProperty("dataResposta")
    @Column(name = "data_resposta")
    private OffsetDateTime dataResposta;
}
