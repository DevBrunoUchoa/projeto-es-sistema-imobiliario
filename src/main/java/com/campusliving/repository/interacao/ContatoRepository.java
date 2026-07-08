package com.campusliving.repository.interacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.interacao.Contato;

public interface ContatoRepository extends JpaRepository<Contato, UUID> {

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
}
