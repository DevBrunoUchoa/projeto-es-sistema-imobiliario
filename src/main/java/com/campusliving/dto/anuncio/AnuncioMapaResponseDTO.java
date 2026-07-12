package com.campusliving.dto.anuncio;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AnuncioMapaResponseDTO {
    private UUID id;
    private Double latitude;
    private Double longitude;
    private BigDecimal preco;
    private String tipo;
}