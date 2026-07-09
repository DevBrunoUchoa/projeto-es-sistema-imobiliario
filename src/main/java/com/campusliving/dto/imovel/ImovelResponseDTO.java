package com.campusliving.dto.imovel;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.imovel.Imovel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImovelResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("proprietarioId")
    private UUID proprietarioId;

    @JsonProperty("tipo")
    private Imovel.Tipo tipo;

    @JsonProperty("cep")
    private String cep;

    @JsonProperty("rua")
    private String rua;

    @JsonProperty("numero")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("ativo")
    private boolean ativo;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    public ImovelResponseDTO(Imovel imovel) {
        this.id = imovel.getId();
        this.proprietarioId = imovel.getProprietarioId();
        this.tipo = imovel.getTipo();
        this.cep = imovel.getCep();
        this.rua = imovel.getRua();
        this.numero = imovel.getNumero();
        this.complemento = imovel.getComplemento();
        this.bairro = imovel.getBairro();
        this.cidade = imovel.getCidade();
        this.estado = imovel.getEstado();
        this.latitude = imovel.getLatitude();
        this.longitude = imovel.getLongitude();
        this.ativo = imovel.isAtivo();
        this.dataCriacao = imovel.getDataCriacao();
    }
}
