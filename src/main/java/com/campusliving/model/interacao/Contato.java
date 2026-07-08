package com.campusliving.model.interacao;

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
 * Mapeia {@code contacts} (ver {@code V12__create_contacts.sql}) — RF-28.
 * É essa entidade que, ao existir para o par (estudante, anúncio de um
 * locador), libera os dados de contato do locador (RNF/LEG-03).
 */
@Entity
@Table(name = "contacts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contato {

    public enum Status {
        ENVIADO,
        LIDO,
        RESPONDIDO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("estudanteId")
    @Column(name = "estudante_id", nullable = false)
    private UUID estudanteId;

    @JsonProperty("adId")
    @Column(name = "ad_id", nullable = false)
    private UUID adId;

    @JsonProperty("mensagem")
    @Column(nullable = false)
    private String mensagem;

    @JsonProperty("status")
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = Status.ENVIADO.name();

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
