package com.campusliving.model.usuario;

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
 * Mapeia {@code verificacao_locador}
 * (ver {@code V4__create_verificacao_locador.sql}) — RF-08/RF-09.
 *
 * <p>Sem relacionamento @ManyToOne para User de propósito: mantemos apenas o
 * UUID cru (userId/analisadoPor) para não acoplar essa entidade a mais
 * complexidade do que o T5.4 precisa (a análise/aprovação por um ADMIN fica
 * para uma tarefa futura de moderação).</p>
 */
@Entity
@Table(name = "verificacao_locador")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificacaoLocador {

    public enum Status {
        PENDENTE,
        APROVADO,
        REJEITADO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("status")
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = Status.PENDENTE.name();

    @JsonProperty("documentoUrl")
    @Column(name = "documento_url", nullable = false, length = 500)
    private String documentoUrl;

    @JsonProperty("analisadoPor")
    @Column(name = "analisado_por")
    private UUID analisadoPor;

    @JsonProperty("justificativaRejeicao")
    @Column(name = "justificativa_rejeicao", length = 500)
    private String justificativaRejeicao;

    // Preenchido pelo banco (DEFAULT now()); igual ao padrão já usado em User.
    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @JsonProperty("dataAnalise")
    @Column(name = "data_analise")
    private OffsetDateTime dataAnalise;
}
