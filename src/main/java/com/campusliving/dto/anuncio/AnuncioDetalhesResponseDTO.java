package com.campusliving.dto.anuncio;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AnuncioDetalhesResponseDTO {
    // Dados do anúncio
    private UUID id;
    private String titulo;
    private String tipoOferta;
    private BigDecimal precoAluguel;
    private BigDecimal precoCondominio;
    private BigDecimal precoIptu;
    private String status;
    private String descricao;
    private Integer vagasTotal;
    private Integer vagasDisponiveis;
    private Integer visualizacoes;
    private OffsetDateTime dataPublicacao;

    // Dados do imóvel
    private UUID imovelId;
    private String tipoImovel;
    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private Double latitude;
    private Double longitude;

    // Distância até a UFCG
    private Integer distanciaUfcgMetros;
    private Integer tempoPeMin;
    private Integer tempoOnibusMin;

    // Imagens
    private List<String> imagens;

    // Avaliações
    private Double notaMedia;
    private Integer totalAvaliacoes;
}