package com.campusliving.dto.imovel;

import java.math.BigDecimal;

import com.campusliving.model.imovel.RegrasCasa;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do PUT /anuncios/:id (RF-13) — atualização parcial: preço, descrição
 * e os campos opcionais de convivência (pets, fumantes, alimentação).
 *
 * <p>"Mobiliado" (citado na RF-13) é uma comodidade do IMÓVEL
 * ({@code property_amenities}), não do anúncio — fora do escopo deste
 * endpoint, que edita {@code ads}/{@code ad_rules}. Editar comodidades do
 * imóvel ficaria num endpoint próprio de imóvel, não implementado aqui por
 * não estar listado nas 7 subtasks do T5.5.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioUpdateRequestDTO {

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("precoAluguel")
    private BigDecimal precoAluguel;

    @JsonProperty("precoCondominio")
    private BigDecimal precoCondominio;

    @JsonProperty("precoIptu")
    private BigDecimal precoIptu;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("aceitaFumantes")
    private Boolean aceitaFumantes;

    @JsonProperty("petFriendly")
    private Boolean petFriendly;

    @JsonProperty("alimentacaoInclusa")
    private RegrasCasa.TipoAlimentacao alimentacaoInclusa;
}
