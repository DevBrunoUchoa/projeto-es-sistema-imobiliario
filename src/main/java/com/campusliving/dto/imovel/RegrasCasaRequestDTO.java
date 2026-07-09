package com.campusliving.dto.imovel;

import com.campusliving.model.imovel.RegrasCasa;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bloco opcional de regras de convivência, usado tanto no POST /anuncios
 * (RF-12) quanto no PUT /anuncios/:id (RF-13). Todos os campos são
 * nullable — no PUT, atualização parcial (mesmo padrão do resto do
 * projeto); no POST, nulos viram os defaults do banco (ver ad_rules, V10).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegrasCasaRequestDTO {

    @JsonProperty("aceitaFumantes")
    private Boolean aceitaFumantes;

    @JsonProperty("petFriendly")
    private Boolean petFriendly;

    @JsonProperty("restricaoGenero")
    private RegrasCasa.RestricaoGenero restricaoGenero;

    @JsonProperty("nivelBarulho")
    private RegrasCasa.NivelBarulho nivelBarulho;

    @JsonProperty("alimentacaoInclusa")
    private RegrasCasa.TipoAlimentacao alimentacaoInclusa;

    @JsonProperty("permiteVisitas")
    private Boolean permiteVisitas;

    @JsonProperty("horarioSilencio")
    private String horarioSilencio;
}
