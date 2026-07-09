package com.campusliving.model.imovel;

import java.math.BigDecimal;
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
 * Mapeia {@code ads} (V9) — RF-12 a RF-17, RF-21 a RF-25. Publicação/edição/
 * status/estatísticas implementados no T5.5; busca textual (search_vector)
 * fica para o T5.6.
 *
 * <p>{@code search_vector} (TSVECTOR gerado pelo próprio Postgres a partir de
 * título+descrição) é DELIBERADAMENTE omitido: o Hibernate não tem um tipo
 * nativo para TSVECTOR e a coluna nunca é escrita pela aplicação mesmo (é
 * {@code GENERATED ALWAYS ... STORED}) — quando o T5.6 implementar a busca
 * textual, ela deve ser feita via query nativa (JPQL/@Query nativeQuery),
 * não via um campo de entidade.</p>
 */
@Entity
@Table(name = "ads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Anuncio {

    public enum TipoOferta {
        IMOVEL_COMPLETO, VAGA_COMPARTILHADA
    }

    // SUSPENSO adicionado em V20 (T5.5.4/RF-14) — inativação lógica
    // administrativa, distinta de INATIVO (decisão do próprio locador).
    public enum Status {
        ATIVO, INATIVO, ALUGADO, SUSPENSO
    }

    @JsonProperty("id")
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonProperty("imovelId")
    @Column(name = "imovel_id", nullable = false)
    private UUID imovelId;

    @JsonProperty("locadorId")
    @Column(name = "locador_id", nullable = false)
    private UUID locadorId;

    @JsonProperty("titulo")
    @Column(nullable = false, length = 150)
    private String titulo;

    @JsonProperty("tipoOferta")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_oferta", nullable = false, length = 30)
    private TipoOferta tipoOferta;

    @JsonProperty("precoAluguel")
    @Column(name = "preco_aluguel", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAluguel;

    @JsonProperty("precoCondominio")
    @Column(name = "preco_condominio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCondominio;

    @JsonProperty("precoIptu")
    @Column(name = "preco_iptu", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoIptu;

    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @JsonProperty("distanciaUfcgMetros")
    @Column(name = "distancia_ufcg_metros")
    private Integer distanciaUfcgMetros;

    @JsonProperty("tempoPeMin")
    @Column(name = "tempo_pe_min")
    private Integer tempoPeMin;

    @JsonProperty("tempoOnibusMin")
    @Column(name = "tempo_onibus_min")
    private Integer tempoOnibusMin;

    @JsonProperty("geoFallback")
    @Column(name = "geo_fallback", nullable = false)
    private boolean geoFallback;

    @JsonProperty("descricao")
    @Column
    private String descricao;

    @JsonProperty("vagasTotal")
    @Column(name = "vagas_total", nullable = false)
    private Integer vagasTotal;

    @JsonProperty("vagasDisponiveis")
    @Column(name = "vagas_disponiveis", nullable = false)
    private Integer vagasDisponiveis;

    @JsonProperty("destaque")
    @Column(nullable = false)
    private boolean destaque;

    @JsonProperty("visualizacoes")
    @Column(nullable = false)
    private Integer visualizacoes;

    @JsonProperty("dataPublicacao")
    @Column(name = "data_publicacao")
    private OffsetDateTime dataPublicacao;

    @JsonProperty("dataExpiracao")
    @Column(name = "data_expiracao")
    private OffsetDateTime dataExpiracao;

    @JsonProperty("dataCriacao")
    @Column(name = "data_criacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @JsonProperty("dataAtualizacao")
    @Column(name = "data_atualizacao", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime dataAtualizacao;
}
