package com.campusliving.dto.anuncio;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnuncioFiltroRequestDTO {
    private BigDecimal precoMax;
    private Integer distanciaMaxMetros;
    private Boolean mobiliado;
    private Boolean permitePets;
    private Boolean permiteFumantes;
    private Boolean incluiAlimentacao;
    private String tipoOferta;
}