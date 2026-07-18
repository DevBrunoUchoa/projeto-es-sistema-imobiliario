package com.campusliving.repository.interacao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.interacao.Contato;

public interface ContatoRepository extends JpaRepository<Contato, UUID> {

    // "Minhas mensagens" — interesses que EU (estudante) registrei.
    List<Contato> findByEstudanteIdOrderByDataCriacaoDesc(UUID estudanteId);

    // "Recebidos" — interesses em qualquer anúncio DESTE locador. Sem
    // @ManyToOne entre Contato e Anuncio de propósito (ver comentário na
    // entidade Contato), então o vínculo é feito via subquery JPQL.
    @Query("SELECT c FROM Contato c WHERE c.adId IN "
            + "(SELECT a.id FROM Anuncio a WHERE a.locadorId = :locadorId) "
            + "ORDER BY c.dataCriacao DESC")
    List<Contato> findRecebidosPorLocador(@Param("locadorId") UUID locadorId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM ads WHERE id = :adId)", nativeQuery = true)
    boolean anuncioExiste(@Param("adId") UUID adId);

    // RNF/LEG-03: dados de contato do locador só são liberados para um
    // estudante que já registrou interesse (contacts) em algum anúncio DESSE
    // locador. Sem entidade JPA para "ads", o join é feito via SQL nativo.
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM contacts c
                JOIN ads a ON a.id = c.ad_id
                WHERE c.estudante_id = :estudanteId
                  AND a.locador_id = :locadorId
            )
            """, nativeQuery = true)
    boolean existeContatoEntre(@Param("estudanteId") UUID estudanteId, @Param("locadorId") UUID locadorId);

    // RNF/LEG-03 (sentido inverso): o estudante liberou o próprio contato ao
    // locador ao registrar interesse (contato_liberado = TRUE) em algum anúncio
    // DESTE locador. É o que autoriza o locador a ver e-mail/telefone do
    // estudante no perfil público.
    @Query(value = """
            SELECT EXISTS(
                SELECT 1
                FROM contacts c
                JOIN ads a ON a.id = c.ad_id
                WHERE c.estudante_id = :estudanteId
                  AND a.locador_id = :locadorId
                  AND c.contato_liberado = TRUE
            )
            """, nativeQuery = true)
    boolean existeContatoLiberadoEntre(@Param("estudanteId") UUID estudanteId, @Param("locadorId") UUID locadorId);
}