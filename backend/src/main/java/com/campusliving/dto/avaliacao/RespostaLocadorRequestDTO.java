package com.campusliving.dto.avaliacao;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corpo do PUT /avaliacoes/{id}/resposta (RF-31). PUT (não POST) de
// propósito: "permite apenas 1 resposta por review (edição posterior
// permitida)" — publicar e editar são a MESMA operação idempotente
// (substituir o valor atual de resposta_locador), não duas rotas separadas.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespostaLocadorRequestDTO {

    @JsonProperty("resposta")
    @NotBlank(message = "resposta e obrigatoria")
    @Size(max = 1000, message = "resposta deve ter no maximo 1000 caracteres")
    private String resposta;
}