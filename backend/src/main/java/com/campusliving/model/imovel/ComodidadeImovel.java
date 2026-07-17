package com.campusliving.model.imovel;

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

/** Mapeia {@code property_amenities} (V6). Entidade "stub" (ver Imovel). */
@Entity
@Table(name = "property_amenities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComodidadeImovel {

    public enum Tipo {
        MOBILIADO, WIFI, AR_CONDICIONADO, GARAGEM, ACADEMIA, LAVANDERIA, INTERNET_INCLUSA
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("propertyId")
    @Column(name = "property_id", nullable = false)
    private UUID propertyId;

    @JsonProperty("nome")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tipo nome;

    @JsonProperty("valor")
    @Column(nullable = false)
    private boolean valor;
}
