package com.campusliving.repository.imovel;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.imovel.RegrasCasa;

public interface RegrasCasaRepository extends JpaRepository<RegrasCasa, UUID> {
}
