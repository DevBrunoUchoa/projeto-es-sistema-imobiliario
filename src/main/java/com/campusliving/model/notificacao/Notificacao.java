package com.campusliving.model.notificacao;

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
 * Mapeia {@code notifications} (V15) — RF-35, RF-38, RF-39. Entidade "stub":
 * o envio/leitura de notificações é acionado por outras tarefas (T5.7 ao
 * responder avaliação, T5.8 ao aceitar match, etc.), ainda não implementado.
 */
@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notificacao {

    public enum Tipo {
        MATCH, MENSAGEM, AVALIACAO, DENUNCIA_RESOLVIDA, VERIFICACAO_APROVADA
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("tipo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo tipo;

    @JsonProperty("titulo")
    @Column(nullable = false, length = 150)
    private String titulo;

    @JsonProperty("mensagem")
    @Column
    private String mensagem;

    @JsonProperty("lida")
    @Column(nullable = false)
    private boolean lida;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
