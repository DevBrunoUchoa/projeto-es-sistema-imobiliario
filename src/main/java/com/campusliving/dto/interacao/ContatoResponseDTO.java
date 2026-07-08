package com.campusliving.dto.interacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.interacao.Contato;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContatoResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("estudanteId")
    private UUID estudanteId;

    @JsonProperty("adId")
    private UUID adId;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("status")
    private String status;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    public ContatoResponseDTO(Contato contato) {
        this.id = contato.getId();
        this.estudanteId = contato.getEstudanteId();
        this.adId = contato.getAdId();
        this.mensagem = contato.getMensagem();
        this.status = contato.getStatus();
        this.dataCriacao = contato.getDataCriacao();
    }
}
