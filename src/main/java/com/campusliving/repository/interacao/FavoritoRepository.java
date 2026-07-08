package com.campusliving.repository.interacao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.interacao.Favorito;

public interface FavoritoRepository extends JpaRepository<Favorito, UUID> {

    List<Favorito> findByUserId(UUID userId);

    boolean existsByUserIdAndAdId(UUID userId, UUID adId);

    void deleteByUserIdAndAdId(UUID userId, UUID adId);

    // Não há entidade JPA para "ads" ainda (isso é T5.5): checagem de
    // existência via SQL nativo direto na tabela, só para não deixar o
    // usuário favoritar um ad_id inexistente (o que geraria uma violação de
    // FK crua / 500 em vez de um 404 tratado).
    @Query(value = "SELECT EXISTS(SELECT 1 FROM ads WHERE id = :adId)", nativeQuery = true)
    boolean anuncioExiste(@Param("adId") UUID adId);
}
