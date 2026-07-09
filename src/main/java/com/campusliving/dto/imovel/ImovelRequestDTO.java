package com.campusliving.dto.imovel;

import com.campusliving.model.imovel.Imovel;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do POST /imoveis (RF-11). Não inclui latitude/longitude
 * propositalmente: o serviço geocodifica o endereço (ver
 * {@code com.campusliving.service.integracao.GeocodingService}) em vez de
 * confiar em coordenadas enviadas pelo cliente — evita imóvel com
 * localização inconsistente com o endereço textual informado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImovelRequestDTO {

    @JsonProperty("tipo")
    @NotNull(message = "tipo e obrigatorio")
    private Imovel.Tipo tipo;

    @JsonProperty("cep")
    @NotBlank(message = "cep e obrigatorio")
    private String cep;

    @JsonProperty("rua")
    @NotBlank(message = "rua e obrigatoria")
    private String rua;

    @JsonProperty("numero")
    @NotBlank(message = "numero e obrigatorio")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    @NotBlank(message = "bairro e obrigatorio")
    private String bairro;

    // Opcionais: RF-11 já restringe o cadastro a Campina Grande-PB via CEP,
    // então default aqui é só conveniência pro cliente não precisar mandar
    // sempre os mesmos dois valores.
    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado;
}
