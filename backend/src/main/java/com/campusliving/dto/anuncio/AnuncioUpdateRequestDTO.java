package com.campusliving.dto.anuncio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AnuncioUpdateRequestDTO {

    @NotBlank(message = "Título é obrigatório")
    private String titulo;

    private String descricao;

    @NotBlank(message = "Tipo de oferta é obrigatório")
    private String tipoOferta;

    @NotNull(message = "Preço do aluguel é obrigatório")
    @Positive(message = "Preço do aluguel deve ser maior que zero")
    private BigDecimal precoAluguel;

    @NotNull(message = "Preço do condomínio é obrigatório")
    @PositiveOrZero(message = "Preço do condomínio deve ser maior ou igual a zero")
    private BigDecimal precoCondominio;

    @NotNull(message = "Preço do IPTU é obrigatório")
    @PositiveOrZero(message = "Preço do IPTU deve ser maior ou igual a zero")
    private BigDecimal precoIptu;

    @NotNull(message = "Total de vagas é obrigatório")
    @Positive(message = "Total de vagas deve ser maior que zero")
    private Integer vagasTotal;

    @NotNull(message = "Vagas disponíveis é obrigatório")
    @PositiveOrZero(message = "Vagas disponíveis deve ser maior ou igual a zero")
    private Integer vagasDisponiveis;

    @NotNull(message = "Data de disponibilidade é obrigatória")
    private LocalDate dataDisponivelDe;

    private LocalDate dataDisponivelAte;

    @Positive(message = "Mínimo de meses deve ser maior que zero")
    private Integer periodoMinMeses;

    @Positive(message = "Máximo de meses deve ser maior que zero")
    private Integer periodoMaxMeses;
}