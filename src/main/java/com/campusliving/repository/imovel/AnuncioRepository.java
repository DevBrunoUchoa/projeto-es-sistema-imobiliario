package com.campusliving.repository.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.imovel.Anuncio;

public interface AnuncioRepository extends JpaRepository<Anuncio, UUID> {
}
