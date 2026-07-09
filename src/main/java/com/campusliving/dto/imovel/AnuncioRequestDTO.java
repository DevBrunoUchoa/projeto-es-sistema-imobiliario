package com.campusliving.dto.imovel;

import java.math.BigDecimal;
import java.util.UUID;

import com.campusliving.model.imovel.Anuncio;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Corpo do POST /anuncios (RF-12). O locadorId vem do requester (X-User-Id), nunca do corpo. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioRequestDTO {

    @JsonProperty("imovelId")
    @NotNull(message = "imovelId e obrigatorio")
    private UUID imovelId;

    @JsonProperty("titulo")
    @NotBlank(message = "titulo e obrigatorio")
    private String titulo;

    @JsonProperty("tipoOferta")
    @NotNull(message = "tipoOferta e obrigatorio")
    private Anuncio.TipoOferta tipoOferta;

    @JsonProperty("precoAluguel")
    @NotNull(message = "precoAluguel e obrigatorio")
    private BigDecimal precoAluguel;

    @JsonProperty("precoCondominio")
    private BigDecimal precoCondominio;

    @JsonProperty("precoIptu")
    private BigDecimal precoIptu;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("vagasTotal")
    private Integer vagasTotal;

    @JsonProperty("regrasCasa")
    private RegrasCasaRequestDTO regrasCasa;
}
