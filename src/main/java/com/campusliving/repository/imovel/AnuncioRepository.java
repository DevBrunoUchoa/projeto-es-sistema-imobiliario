package com.campusliving.repository.imovel;

import com.campusliving.model.imovel.Anuncio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AnuncioRepository extends JpaRepository<Anuncio, UUID> {

    List<Anuncio> findByImovelIdAndStatus(UUID imovelId, Anuncio.Status status);

    /**
     * RF-16: distância em linha reta (metros) entre o imóvel e a UFCG, via
     * PostGIS (ST_Distance em geography). Retorna null se o imóvel não tiver
     * geometria (endereço não geocodificado).
     */
    @Query(value = "SELECT ROUND(ST_Distance(p.geom::geography, "
            + "ST_SetSRID(ST_MakePoint(:ufcgLon, :ufcgLat), 4326)::geography))::int "
            + "FROM properties p WHERE p.id = :imovelId AND p.geom IS NOT NULL",
            nativeQuery = true)
    Integer calcularDistanciaUfcgMetros(@Param("imovelId") UUID imovelId,
            @Param("ufcgLat") double ufcgLat, @Param("ufcgLon") double ufcgLon);

    List<Anuncio> findByStatus(Anuncio.Status status);

    Page<Anuncio> findByStatus(Anuncio.Status status, Pageable pageable);

    long countByStatus(Anuncio.Status status);

    @Query("SELECT a FROM Anuncio a " +
           "JOIN Imovel i ON a.imovelId = i.id " +
           "WHERE a.status = :status " +
           "AND (:precoMax IS NULL OR a.precoAluguel <= :precoMax) " +
           "AND (:distanciaMaxMetros IS NULL OR a.distanciaUfcgMetros <= :distanciaMaxMetros) " +
           "AND (:mobiliado IS NULL OR i.mobiliado = :mobiliado) " +
           "AND (:permitePets IS NULL OR i.permitePets = :permitePets) " +
           "AND (:permiteFumantes IS NULL OR i.permiteFumantes = :permiteFumantes) " +
           "AND (:incluiAlimentacao IS NULL OR i.incluiAlimentacao = :incluiAlimentacao) " +
           "AND (:tipoOferta IS NULL OR a.tipoOferta = :tipoOferta)")
    Page<Anuncio> findByFiltros(
            @Param("status") Anuncio.Status status,
            @Param("precoMax") BigDecimal precoMax,
            @Param("distanciaMaxMetros") Integer distanciaMaxMetros,
            @Param("mobiliado") Boolean mobiliado,
            @Param("permitePets") Boolean permitePets,
            @Param("permiteFumantes") Boolean permiteFumantes,
            @Param("incluiAlimentacao") Boolean incluiAlimentacao,
            @Param("tipoOferta") String tipoOferta,
            Pageable pageable
    );

    @Query("SELECT a FROM Anuncio a " +
           "WHERE a.status = :status " +
           "AND (:query IS NULL OR " +
           "   LOWER(a.titulo) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "   LOWER(a.descricao) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Anuncio> buscarPorTexto(
            @Param("status") Anuncio.Status status,
            @Param("query") String query,
            Pageable pageable
    );
}