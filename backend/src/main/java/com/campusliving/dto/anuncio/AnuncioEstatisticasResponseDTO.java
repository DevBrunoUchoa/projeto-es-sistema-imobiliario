package com.campusliving.dto.anuncio;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class AnuncioEstatisticasResponseDTO {
    private Long totalVisualizacoes;
    private Map<LocalDate, Long> visualizacoesPorDia;
}