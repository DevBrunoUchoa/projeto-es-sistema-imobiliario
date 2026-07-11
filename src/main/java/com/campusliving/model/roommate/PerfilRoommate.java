package com.campusliving.model.roommate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeia {@code roommate_profiles} (V7, V19) — RF-10/RF-32.
 */
@Entity
@Table(name = "roommate_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilRoommate {

    public enum NivelBarulho {
        SILENCIOSO, MODERADO, AGITADO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @JsonProperty("descricao")
    @Column
    private String descricao;

    @JsonProperty("orcamentoMax")
    @Column(name = "orcamento_max", precision = 10, scale = 2)
    private BigDecimal orcamentoMax;

    @JsonProperty("dataEntradaDesejada")
    @Column(name = "data_entrada_desejada")
    private LocalDate dataEntradaDesejada;

    @JsonProperty("periodoMinMeses")
    @Column(name = "periodo_min_meses")
    private Integer periodoMinMeses;

    @JsonProperty("aceitaPets")
    @Column(name = "aceita_pets", nullable = false)
    private boolean aceitaPets;

    @JsonProperty("fumante")
    @Column(nullable = false)
    private boolean fumante;

    @JsonProperty("nivelBarulhoPreferido")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_barulho_preferido", length = 20)
    private NivelBarulho nivelBarulhoPreferido;

    @JsonProperty("horarioAcorda")
    @Column(name = "horario_acorda")
    private LocalTime horarioAcorda;

    @JsonProperty("horarioDorme")
    @Column(name = "horario_dorme")
    private LocalTime horarioDorme;

    @JsonProperty("ativo")
    @Column(nullable = false)
    private boolean ativo;

    // RF-32: adicionados em V19__add_roommate_profile_visibility.sql.
    @JsonProperty("jaPossuiCasa")
    @Column(name = "ja_possui_casa", nullable = false)
    private boolean jaPossuiCasa;

    @JsonProperty("perfilVisivel")
    @Column(name = "perfil_visivel", nullable = false)
    private boolean perfilVisivel;

    @JsonProperty("dataAtualizacao")
    @Column(name = "data_atualizacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataAtualizacao;
}
