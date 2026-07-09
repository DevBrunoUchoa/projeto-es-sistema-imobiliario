package com.campusliving.repository.imovel;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.imovel.ImagemAnuncio;

public interface ImagemAnuncioRepository extends JpaRepository<ImagemAnuncio, UUID> {

    /** RF-15: URLs de imagens do anúncio, na ordem de exibição. */
    List<ImagemAnuncio> findByAdIdOrderByOrdemAsc(UUID adId);
}
