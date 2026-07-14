package com.campusliving.model.denuncia;

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
 * Mapeia {@code reports} (V16) — RF-36/RF-37 (painel admin, RF-40 a RF-43).
 * Entidade "stub", deliberadamente sem controller/service: fora do escopo do
 * T5 (backend core) — a tabela existe desde já só para o ER ficar completo.
 *
 * <p>{@code alvoId} referencia {@code ads.id} OU {@code users.id} dependendo
 * de {@code tipoAlvo} — por isso não é uma FK gerenciada pelo Hibernate,
 * igual já é o caso no schema (ver comentário em V16).</p>
 */
@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Denuncia {

    public enum TipoAlvo {
        ANUNCIO, USUARIO
    }

    public enum Motivo {
        CONTEUDO_INADEQUADO, SPAM, FRAUDE, ASSEDIO, OUTROS
    }

    public enum Status {
        PENDENTE, EM_ANALISE, RESOLVIDA, REJEITADA
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("denuncianteId")
    @Column(name = "denunciante_id", nullable = false)
    private UUID denuncianteId;

    @JsonProperty("tipoAlvo")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alvo", nullable = false, length = 20)
    private TipoAlvo tipoAlvo;

    @JsonProperty("alvoId")
    @Column(name = "alvo_id", nullable = false)
    private UUID alvoId;

    @JsonProperty("motivo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Motivo motivo;

    @JsonProperty("descricao")
    @Column
    private String descricao;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @JsonProperty("resolvidoPor")
    @Column(name = "resolvido_por")
    private UUID resolvidoPor;

    @JsonProperty("contadorDenuncias")
    @Column(name = "contador_denuncias", nullable = false)
    private Integer contadorDenuncias;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @JsonProperty("dataResolucao")
    @Column(name = "data_resolucao")
    private OffsetDateTime dataResolucao;
}
