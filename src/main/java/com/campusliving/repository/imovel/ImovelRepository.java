package com.campusliving.repository.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.imovel.Imovel;

public interface ImovelRepository extends JpaRepository<Imovel, UUID> {

    /**
     * RF-16: distância em linha reta (metros) entre o imóvel e um ponto de
     * referência (a UFCG), via PostGIS. Query nativa em vez de mapear
     * {@code geom} no Hibernate (que exigiria a dependência extra
     * hibernate-spatial — ver comentário em {@code Imovel}): fazemos o
     * cast pra {@code geography} pra obter distância real em metros
     * (considerando a curvatura da Terra) em vez do resultado em graus que
     * {@code ST_Distance} daria sobre {@code geometry} puro.
     *
     * <p>É DELIBERADAMENTE uma distância em linha reta, não uma rota real a
     * pé/de ônibus (isso exigiria um serviço de roteamento externo, fora do
     * escopo decidido para este momento do T5.5) — por isso o resultado
     * alimenta {@code Anuncio.geoFallback = true} em
     * {@code AnuncioGeoService}.</p>
     */
    @Query(value = "SELECT ST_Distance(geom::geography, "
            + "ST_SetSRID(ST_MakePoint(:lonRef, :latRef), 4326)::geography) "
            + "FROM properties WHERE id = :imovelId", nativeQuery = true)
    Double calcularDistanciaMetros(@Param("imovelId") UUID imovelId,
                                    @Param("latRef") double latRef,
                                    @Param("lonRef") double lonRef);
}
