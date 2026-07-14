package com.campusliving.repository.avaliacao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.avaliacao.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

    // RF-29: "bloquear mais de uma avaliação por par (avaliador, anuncio)".
    // Checagem prévia no service — a garantia definitiva é o índice único
    // parcial uq_reviews_avaliador_ad (V13__create_reviews.sql), que também
    // cobre a corrida entre requisições concorrentes que esta checagem sozinha
    // não cobre.
    boolean existsByAvaliadorIdAndAdId(UUID avaliadorId, UUID adId);

        // Listagem de avaliações de um anúncio específico (tela de detalhe, RF-15).
    Page<Avaliacao> findByAdId(UUID adId, Pageable pageable);

    // Listagem de todas as avaliações recebidas por um locador, agregando
    // todos os anúncios dele (tela de perfil público, RF-07).
    Page<Avaliacao> findByAvaliadoId(UUID avaliadoId, Pageable pageable);
}
