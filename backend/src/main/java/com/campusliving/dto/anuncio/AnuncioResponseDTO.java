package com.campusliving.dto.anuncio;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AnuncioResponseDTO {
    private UUID id;
    private UUID imovelId;
    private UUID locadorId;
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
    private LocalDate dataDisponivelDe;
    private LocalDate dataDisponivelAte;
    private Integer periodoMinMeses;
    private Integer periodoMaxMeses;
    private String mensagem;
}