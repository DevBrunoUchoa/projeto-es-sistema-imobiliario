package com.campusliving.dto.imovel;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Corpo do PATCH /anuncios/:id/status (RF-14). Valores aceitos: ATIVO/INATIVO/SUSPENSO. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioStatusUpdateDTO {

    @JsonProperty("status")
    @NotNull(message = "status e obrigatorio")
    private String status;
}
