package com.campusliving.model.avaliacao;

import java.time.OffsetDateTime;
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
 * Mapeia {@code reviews} (V13) — RF-29 a RF-31. Entidade "stub": estrutura de
 * dados para o T5.2 ficar completo; publicar/responder/validar permissão
 * ficam para o T5.7.
 *
 * <p>{@code avaliadorId}/{@code avaliadoId} são nullable de propósito: viram
 * NULL quando o usuário correspondente pede exclusão permanente da conta
 * (RNF/LEG-02 — ver ON DELETE SET NULL na migration), preservando a
 * avaliação em si de forma anonimizada.</p>
 */
@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("avaliadorId")
    @Column(name = "avaliador_id")
    private UUID avaliadorId;

    @JsonProperty("avaliadoId")
    @Column(name = "avaliado_id")
    private UUID avaliadoId;

    @JsonProperty("adId")
    @Column(name = "ad_id", nullable = false)
    private UUID adId;

    @JsonProperty("nota")
    @Column(nullable = false)
    private Short nota;

    @JsonProperty("comentario")
    @Column
    private String comentario;

    @JsonProperty("respostaLocador")
    @Column(name = "resposta_locador")
    private String respostaLocador;

    @JsonProperty("contatoPrevio")
    @Column(name = "contato_previo", nullable = false)
    private boolean contatoPrevio;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
