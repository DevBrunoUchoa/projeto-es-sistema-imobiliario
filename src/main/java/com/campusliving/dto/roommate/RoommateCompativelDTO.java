package com.campusliving.dto.roommate;

import java.math.BigDecimal;
import java.util.UUID;

import com.campusliving.model.roommate.PerfilRoommate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Item de GET /roommates/compativeis (RF-33): perfil + score calculado. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateCompativelDTO {

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("orcamentoMax")
    private BigDecimal orcamentoMax;

    @JsonProperty("nivelBarulhoPreferido")
    private PerfilRoommate.NivelBarulho nivelBarulhoPreferido;

    @JsonProperty("jaPossuiCasa")
    private boolean jaPossuiCasa;

    // 0-100. Calculado em RoommateServiceImpl#calcularCompatibilidade.
    @JsonProperty("scoreCompatibilidade")
    private int scoreCompatibilidade;
}
