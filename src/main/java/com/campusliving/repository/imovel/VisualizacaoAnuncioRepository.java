package com.campusliving.repository.imovel;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.model.imovel.VisualizacaoAnuncio;

public interface VisualizacaoAnuncioRepository extends JpaRepository<VisualizacaoAnuncio, UUID> {

    /**
     * RF-17: incrementa em 1 o contador do dia corrente para o anúncio,
     * criando a linha (anúncio, hoje) se ainda não existir. Upsert nativo
     * porque o JPA não tem uma forma direta de "insere ou incrementa" sem
     * antes carregar a linha (o que criaria uma corrida entre duas
     * visualizações simultâneas) — a constraint UNIQUE
     * (uq_ad_views_ad_dia, V21) garante a atomicidade real no banco.
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO ad_views (ad_id, data_visualizacao, quantidade) "
            + "VALUES (:adId, CURRENT_DATE, 1) "
            + "ON CONFLICT (ad_id, data_visualizacao) "
            + "DO UPDATE SET quantidade = ad_views.quantidade + 1", nativeQuery = true)
    void registrarVisualizacao(@Param("adId") UUID adId);

    List<VisualizacaoAnuncio> findByAdIdOrderByDataVisualizacaoAsc(UUID adId);
}
