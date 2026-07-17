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
 * Mapeia {@code favorites} (ver {@code V14__create_favorites.sql}) —
 * RF-26/RF-27.
 *
 * <p>{@code adId} é um UUID cru (sem @ManyToOne) porque a entidade JPA de
 * anúncio ainda não existe (T5.5). A unicidade (user_id, ad_id) e a
 * existência do anúncio são garantidas pelo banco / por query nativa
 * respectivamente — ver o repository de Favorito.</p>
 */
@Entity
@Table(name = "favorites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorito {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("userId")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @JsonProperty("adId")
    @Column(name = "ad_id", nullable = false)
    private UUID adId;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
