package com.campusliving.repository.avaliacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.avaliacao.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    /**
     * RF-15: nota média do anúncio. Retorna {@code null} (via AVG do SQL)
     * quando não há nenhuma avaliação ainda — tratado no service.
     */
    @Query("SELECT AVG(a.nota) FROM Avaliacao a WHERE a.adId = :adId")
    Double calcularNotaMedia(@Param("adId") UUID adId);
}
