package com.campusliving.dto.anuncio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnuncioRequestDTO {

    @NotNull(message = "ID do imóvel é obrigatório")
    private String imovelId;

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    @NotBlank(message = "Tipo de oferta é obrigatório")
    private String tipoOferta; // IMOVEL_COMPLETO, VAGA_COMPARTILHADA

    @NotNull(message = "Preço do aluguel é obrigatório")
    @Positive(message = "Preço do aluguel deve ser maior que zero")
    private BigDecimal precoAluguel;

    @NotNull(message = "Preço do condomínio é obrigatório")
    @PositiveOrZero(message = "Preço do condomínio deve ser maior ou igual a zero")
    private BigDecimal precoCondominio;

    @NotNull(message = "Preço do IPTU é obrigatório")
    @PositiveOrZero(message = "Preço do IPTU deve ser maior ou igual a zero")
    private BigDecimal precoIptu;

    private String descricao;

    @NotNull(message = "Total de vagas é obrigatório")
    @Positive(message = "Total de vagas deve ser maior que zero")
    private Integer vagasTotal;

    @NotNull(message = "Vagas disponíveis é obrigatório")
    @PositiveOrZero(message = "Vagas disponíveis deve ser maior ou igual a zero")
    private Integer vagasDisponiveis;
}