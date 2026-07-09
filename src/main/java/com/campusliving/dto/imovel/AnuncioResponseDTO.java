package com.campusliving.dto.imovel;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.imovel.Anuncio;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("imovelId")
    private UUID imovelId;

    @JsonProperty("locadorId")
    private UUID locadorId;

    @JsonProperty("titulo")
    private String titulo;

    @JsonProperty("tipoOferta")
    private Anuncio.TipoOferta tipoOferta;

    @JsonProperty("precoAluguel")
    private BigDecimal precoAluguel;

    @JsonProperty("precoCondominio")
    private BigDecimal precoCondominio;

    @JsonProperty("precoIptu")
    private BigDecimal precoIptu;

    @JsonProperty("status")
    private String status;

    @JsonProperty("distanciaUfcgMetros")
    private Integer distanciaUfcgMetros;

    @JsonProperty("tempoPeMin")
    private Integer tempoPeMin;

    @JsonProperty("tempoOnibusMin")
    private Integer tempoOnibusMin;

    @JsonProperty("geoFallback")
    private boolean geoFallback;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("vagasTotal")
    private Integer vagasTotal;

    @JsonProperty("vagasDisponiveis")
    private Integer vagasDisponiveis;

    @JsonProperty("destaque")
    private boolean destaque;

    @JsonProperty("visualizacoes")
    private Integer visualizacoes;

    @JsonProperty("dataPublicacao")
    private OffsetDateTime dataPublicacao;

    @JsonProperty("dataExpiracao")
    private OffsetDateTime dataExpiracao;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    @JsonProperty("dataAtualizacao")
    private OffsetDateTime dataAtualizacao;

    public AnuncioResponseDTO(Anuncio anuncio) {
        this.id = anuncio.getId();
        this.imovelId = anuncio.getImovelId();
        this.locadorId = anuncio.getLocadorId();
        this.titulo = anuncio.getTitulo();
        this.tipoOferta = anuncio.getTipoOferta();
        this.precoAluguel = anuncio.getPrecoAluguel();
        this.precoCondominio = anuncio.getPrecoCondominio();
        this.precoIptu = anuncio.getPrecoIptu();
        this.status = anuncio.getStatus() != null ? anuncio.getStatus().name() : null;
        this.distanciaUfcgMetros = anuncio.getDistanciaUfcgMetros();
        this.tempoPeMin = anuncio.getTempoPeMin();
        this.tempoOnibusMin = anuncio.getTempoOnibusMin();
        this.geoFallback = anuncio.isGeoFallback();
        this.descricao = anuncio.getDescricao();
        this.vagasTotal = anuncio.getVagasTotal();
        this.vagasDisponiveis = anuncio.getVagasDisponiveis();
        this.destaque = anuncio.isDestaque();
        this.visualizacoes = anuncio.getVisualizacoes();
        this.dataPublicacao = anuncio.getDataPublicacao();
        this.dataExpiracao = anuncio.getDataExpiracao();
        this.dataCriacao = anuncio.getDataCriacao();
        this.dataAtualizacao = anuncio.getDataAtualizacao();
    }
}
