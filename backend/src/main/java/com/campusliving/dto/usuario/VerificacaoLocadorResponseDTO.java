package com.campusliving.dto.usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.usuario.VerificacaoLocador;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoLocadorResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("documentoUrl")
    private String documentoUrl;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    public VerificacaoLocadorResponseDTO(VerificacaoLocador verificacao) {
        this.id = verificacao.getId();
        this.userId = verificacao.getUserId();
        this.status = verificacao.getStatus();
        this.documentoUrl = verificacao.getDocumentoUrl();
        this.dataCriacao = verificacao.getDataCriacao();
    }
}
