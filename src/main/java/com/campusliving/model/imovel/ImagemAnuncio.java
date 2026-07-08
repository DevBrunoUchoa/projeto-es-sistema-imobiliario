package com.campusliving.model.imovel;

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

/** Mapeia {@code ad_images} (V11) — RF-19. Entidade "stub" (ver Imovel). */
@Entity
@Table(name = "ad_images")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagemAnuncio {

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("adId")
    @Column(name = "ad_id", nullable = false)
    private UUID adId;

    @JsonProperty("url")
    @Column(nullable = false, length = 500)
    private String url;

    @JsonProperty("ordem")
    @Column(nullable = false)
    private Integer ordem;

    @JsonProperty("principal")
    @Column(nullable = false)
    private boolean principal;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}
