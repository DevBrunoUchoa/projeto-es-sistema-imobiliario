package com.campusliving.repository.imovel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.campusliving.model.imovel.ImagemAnuncio;

public interface ImagemAnuncioRepository extends JpaRepository<ImagemAnuncio, UUID> {
    List<ImagemAnuncio> findByAdIdOrderByOrdemAsc(UUID adId);
    Optional<ImagemAnuncio> findByIdAndAdId(UUID id, UUID adId);
    long countByAdId(UUID adId);
    @Modifying
    @Query("update ImagemAnuncio i set i.principal = false where i.adId = :adId and i.principal = true")
    void clearPrincipalByAdId(UUID adId);
}
