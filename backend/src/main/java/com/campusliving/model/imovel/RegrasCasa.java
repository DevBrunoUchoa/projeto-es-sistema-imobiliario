package com.campusliving.model.imovel;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mapeia {@code ad_rules} (V10) — relação 1:1 com {@code ads} (a própria PK é
 * a FK para {@code ads.id}, por isso NÃO tem {@code @GeneratedValue}: quem
 * cria o registro deve setar {@code adId} explicitamente igual ao id do
 * anúncio). Entidade "stub" (ver Imovel).
 */
@Entity
@Table(name = "ad_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegrasCasa {

    public enum RestricaoGenero {
        SEM_RESTRICAO, MASCULINO, FEMININO
    }

    public enum NivelBarulho {
        SILENCIOSO, MODERADO, AGITADO
    }

    public enum TipoAlimentacao {
        NENHUMA, CAFE, ALMOCO, JANTAR, COMPLETA
    }

    @JsonProperty("adId")
    @Id
    @Column(name = "ad_id")
    private UUID adId;

    @JsonProperty("aceitaFumantes")
    @Column(name = "aceita_fumantes", nullable = false)
    private boolean aceitaFumantes;

    @JsonProperty("petFriendly")
    @Column(name = "pet_friendly", nullable = false)
    private boolean petFriendly;

    @JsonProperty("restricaoGenero")
    @Enumerated(EnumType.STRING)
    @Column(name = "restricao_genero", nullable = false, length = 20)
    private RestricaoGenero restricaoGenero;

    @JsonProperty("nivelBarulho")
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_barulho", length = 20)
    private NivelBarulho nivelBarulho;

    @JsonProperty("alimentacaoInclusa")
    @Enumerated(EnumType.STRING)
    @Column(name = "alimentacao_inclusa", nullable = false, length = 20)
    private TipoAlimentacao alimentacaoInclusa;

    @JsonProperty("permiteVisitas")
    @Column(name = "permite_visitas", nullable = false)
    private boolean permiteVisitas;

    @JsonProperty("horarioSilencio")
    @Column(name = "horario_silencio", length = 50)
    private String horarioSilencio;
}
