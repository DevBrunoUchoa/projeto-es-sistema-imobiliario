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
 * Mapeia {@code password_reset_tokens} (V3). RF-04.
 *
 * <p>Entidade "stub": só a estrutura de dados, sem service/controller —
 * a lógica de emissão/validação de token é escopo do T5.3 (autenticação).</p>
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("tokenHash")
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    @JsonProperty("expiraEm")
    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @JsonProperty("usado")
    @Column(nullable = false)
    private boolean usado;
}
