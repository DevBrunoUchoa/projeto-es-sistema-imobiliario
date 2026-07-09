package com.campusliving.model.imovel;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeia {@code ad_views} (V21) — RF-17: contagem de visualizações de um
 * anúncio agrupada por dia. No máximo 1 linha por par (anúncio, dia); o
 * incremento é feito via upsert nativo (ver
 * {@code VisualizacaoAnuncioRepository#registrarVisualizacao}), não pelo
 * ciclo de vida normal do JPA (save/update de uma entidade já carregada).
 */
@Entity
@Table(name = "ad_views")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizacaoAnuncio {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("adId")
    @Column(name = "ad_id", nullable = false)
    private UUID adId;

    @JsonProperty("dataVisualizacao")
    @Column(name = "data_visualizacao", nullable = false)
    private LocalDate dataVisualizacao;

    @JsonProperty("quantidade")
    @Column(nullable = false)
    private Integer quantidade;
}
