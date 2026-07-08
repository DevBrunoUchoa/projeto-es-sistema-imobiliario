package com.campusliving.model.auditoria;

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
 * Mapeia {@code audit_logs} (V17) — RNF/SEG-06. Entidade "stub": a tabela é
 * append-only por trigger (ver V17), mas quem realmente grava aqui
 * ({@code AuditLogService}) é escopo do T5.3.
 *
 * <p>{@code dados_anteriores}/{@code dados_novos} (JSONB no banco) são
 * DELIBERADAMENTE omitidos aqui: mapear JSONB corretamente no Hibernate 6
 * exige {@code @JdbcTypeCode(SqlTypes.JSON)} sobre um tipo estruturado
 * (ex.: {@code Map<String,Object>}), o que só faz sentido decidir junto do
 * serviço que vai realmente popular esses campos (T5.3). Mapear como
 * {@code String} agora falharia a validação de schema do Hibernate (tipo de
 * coluna JSONB != VARCHAR esperado).</p>
 */
@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id")
    private UUID userId;

    @JsonProperty("acao")
    @Column(nullable = false, length = 100)
    private String acao;

    @JsonProperty("entidade")
    @Column(nullable = false, length = 100)
    private String entidade;

    @JsonProperty("entidadeId")
    @Column(name = "entidade_id")
    private UUID entidadeId;

    @JsonProperty("ipAddress")
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
