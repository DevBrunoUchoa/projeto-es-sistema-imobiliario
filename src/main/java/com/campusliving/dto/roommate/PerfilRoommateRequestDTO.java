package com.campusliving.dto.roommate;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do POST /roommates/perfil (RF-32) — "ativar card público no mural".
 *
 * <p>Também aceita os campos descritivos do perfil (descrição, orçamento,
 * data de entrada, período mínimo) como opcionais: sem isso, essas colunas
 * nunca teriam por onde ser preenchidas, já que o PUT de preferências
 * (T5.8.1) só cobre os campos de hábito. Upsert: cria o perfil se não
 * existir, ou atualiza + ativa se já existir.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRoommateRequestDTO {

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("orcamentoMax")
    private BigDecimal orcamentoMax;

    @JsonProperty("dataEntradaDesejada")
    private LocalDate dataEntradaDesejada;

    @JsonProperty("periodoMinMeses")
    private Integer periodoMinMeses;

    @JsonProperty("jaPossuiCasa")
    private boolean jaPossuiCasa;

    @JsonProperty("perfilVisivel")
    private boolean perfilVisivel;
}
