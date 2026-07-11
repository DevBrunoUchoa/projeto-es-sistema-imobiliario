package com.campusliving.dto.roommate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.roommate.PerfilRoommate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRoommateResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("orcamentoMax")
    private BigDecimal orcamentoMax;

    @JsonProperty("dataEntradaDesejada")
    private LocalDate dataEntradaDesejada;

    @JsonProperty("periodoMinMeses")
    private Integer periodoMinMeses;

    @JsonProperty("aceitaPets")
    private boolean aceitaPets;

    @JsonProperty("fumante")
    private boolean fumante;

    @JsonProperty("nivelBarulhoPreferido")
    private PerfilRoommate.NivelBarulho nivelBarulhoPreferido;

    @JsonProperty("horarioAcorda")
    private LocalTime horarioAcorda;

    @JsonProperty("horarioDorme")
    private LocalTime horarioDorme;

    @JsonProperty("jaPossuiCasa")
    private boolean jaPossuiCasa;

    @JsonProperty("perfilVisivel")
    private boolean perfilVisivel;

    @JsonProperty("ativo")
    private boolean ativo;

    @JsonProperty("dataAtualizacao")
    private OffsetDateTime dataAtualizacao;

    public PerfilRoommateResponseDTO(PerfilRoommate perfil) {
        this.id = perfil.getId();
        this.userId = perfil.getUserId();
        this.descricao = perfil.getDescricao();
        this.orcamentoMax = perfil.getOrcamentoMax();
        this.dataEntradaDesejada = perfil.getDataEntradaDesejada();
        this.periodoMinMeses = perfil.getPeriodoMinMeses();
        this.aceitaPets = perfil.isAceitaPets();
        this.fumante = perfil.isFumante();
        this.nivelBarulhoPreferido = perfil.getNivelBarulhoPreferido();
        this.horarioAcorda = perfil.getHorarioAcorda();
        this.horarioDorme = perfil.getHorarioDorme();
        this.jaPossuiCasa = perfil.isJaPossuiCasa();
        this.perfilVisivel = perfil.isPerfilVisivel();
        this.ativo = perfil.isAtivo();
        this.dataAtualizacao = perfil.getDataAtualizacao();
    }
}
