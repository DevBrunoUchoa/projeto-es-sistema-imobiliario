package com.campusliving.repository.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.imovel.Anuncio;

public interface AnuncioRepository extends JpaRepository<Anuncio, UUID> {

    /** RF-12: bloquear duplicidade de anúncio ativo por imóvel. */
    boolean existsByImovelIdAndStatus(UUID imovelId, Anuncio.Status status);

    /** RF-14: mesma checagem, mas ignorando o próprio anúncio (reativação). */
    boolean existsByImovelIdAndStatusAndIdNot(UUID imovelId, Anuncio.Status status, UUID id);
}
