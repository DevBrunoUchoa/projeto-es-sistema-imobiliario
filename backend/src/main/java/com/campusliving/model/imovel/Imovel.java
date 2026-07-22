 package com.campusliving.model.imovel;

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
 * Mapeia {@code properties} (V5) — RF-11. Entidade "stub": estrutura de
 * dados para o T5.2 ficar completo; regras de negócio (cadastro, cálculo de
 * distância, geocodificação) ficam para o T5.5.
 *
 * <p>A coluna {@code geom} (PostGIS {@code geometry(Point,4326)}, sincronizada
 * por trigger a partir de latitude/longitude) é DELIBERADAMENTE omitida
 * aqui: mapear tipos geoespaciais no Hibernate exige o módulo
 * hibernate-spatial (dependência extra, ainda não adicionada ao pom) — quando
 * o T5.5 precisar rodar queries espaciais (ST_DWithin/ST_Distance), adicione
 * a dependência e o campo juntos.</p>
 */
@Entity
@Table(name = "properties")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Imovel {

    public enum Tipo {
        APARTAMENTO, QUARTO, FLAT, PENSIONATO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("proprietarioId")
    @Column(name = "proprietario_id", nullable = false)
    private UUID proprietarioId;

    @JsonProperty("tipo")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @JsonProperty("cep")
    @Column(nullable = false, length = 9)
    private String cep;

    @JsonProperty("rua")
    @Column(nullable = false, length = 200)
    private String rua;

    @JsonProperty("numero")
    @Column(nullable = false, length = 20)
    private String numero;

    @JsonProperty("complemento")
    @Column(length = 100)
    private String complemento;

    @JsonProperty("bairro")
    @Column(nullable = false, length = 100)
    private String bairro;

    @JsonProperty("cidade")
    @Column(nullable = false, length = 100)
    private String cidade;

    @JsonProperty("estado")
    @Column(nullable = false, length = 2)
    private String estado;

    @JsonProperty("latitude")
    @Column(nullable = false)
    private Double latitude;

    @JsonProperty("longitude")
    @Column(nullable = false)
    private Double longitude;

    @JsonProperty("ativo")
    @Column(nullable = false)
    private boolean ativo;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @JsonProperty("mobiliado")
    @Column(nullable = false)
    private boolean mobiliado;

    @JsonProperty("permitePets")
    @Column(name = "permite_pets", nullable = false)
    private boolean permitePets;

    @JsonProperty("permiteFumantes")
    @Column(name = "permite_fumantes", nullable = false)
    private boolean permiteFumantes;

    @JsonProperty("incluiAlimentacao")
    @Column(name = "inclui_alimentacao", nullable = false)
    private boolean incluiAlimentacao;

    @JsonProperty("seguranca24h")
    @Column(name = "seguranca_24h", nullable = false)
    private boolean seguranca24h;

    @JsonProperty("lavanderia")
    @Column(nullable = false)
    private boolean lavanderia;

    @JsonProperty("internetInclusa")
    @Column(name = "internet_inclusa", nullable = false)
    private boolean internetInclusa;

    @JsonProperty("mercadinhoProximo")
    @Column(name = "mercadinho_proximo", nullable = false)
    private boolean mercadinhoProximo;

    @JsonProperty("gasIncluso")
    @Column(name = "gas_incluso", nullable = false)
    private boolean gasIncluso;

    @JsonProperty("vagaGaragem")
    @Column(name = "vaga_garagem", nullable = false)
    private boolean vagaGaragem;
}
