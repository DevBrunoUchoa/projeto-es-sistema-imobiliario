package com.campusliving.dto.roommate;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.roommate.RoommateMatch;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateMatchResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("solicitanteId")
    private UUID solicitanteId;

    @JsonProperty("destinatarioId")
    private UUID destinatarioId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("mensagemInicial")
    private String mensagemInicial;

    @JsonProperty("dataSolicitacao")
    private OffsetDateTime dataSolicitacao;

    @JsonProperty("dataResposta")
    private OffsetDateTime dataResposta;

    public RoommateMatchResponseDTO(RoommateMatch match) {
        this.id = match.getId();
        this.solicitanteId = match.getSolicitanteId();
        this.destinatarioId = match.getDestinatarioId();
        this.status = match.getStatus() != null ? match.getStatus().name() : null;
        this.mensagemInicial = match.getMensagemInicial();
        this.dataSolicitacao = match.getDataSolicitacao();
        this.dataResposta = match.getDataResposta();
    }
}
