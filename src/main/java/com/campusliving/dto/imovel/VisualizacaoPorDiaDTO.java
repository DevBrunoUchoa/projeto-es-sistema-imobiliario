package com.campusliving.dto.imovel;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Item de GET /anuncios/:id/estatisticas (RF-17). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizacaoPorDiaDTO {

    @JsonProperty("data")
    private LocalDate data;

    @JsonProperty("quantidade")
    private Integer quantidade;
}
