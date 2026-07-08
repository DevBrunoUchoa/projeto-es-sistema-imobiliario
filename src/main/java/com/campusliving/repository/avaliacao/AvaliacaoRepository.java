package com.campusliving.repository.avaliacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.avaliacao.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {
}
