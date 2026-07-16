package com.campusliving.service.avaliacao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.campusliving.dto.avaliacao.AvaliacaoRequestDTO;
import com.campusliving.dto.avaliacao.AvaliacaoResponseDTO;
import com.campusliving.dto.avaliacao.RespostaLocadorRequestDTO;

public interface AvaliacaoService {

    /** RF-29: publica uma avaliação de anúncio/locador. */
    AvaliacaoResponseDTO publicar(AvaliacaoRequestDTO dto, UUID avaliadorId);

    /** RF-31: registra (ou edita) a resposta do locador a uma avaliação. */
    AvaliacaoResponseDTO responder(UUID avaliacaoId, RespostaLocadorRequestDTO dto, UUID locadorId);

    /** Avaliações recebidas por um anúncio específico (RF-15). */
    Page<AvaliacaoResponseDTO> listarPorAnuncio(UUID adId, Pageable pageable);

    /** Avaliações recebidas por um locador, somando todos os anúncios dele (RF-07). */
    Page<AvaliacaoResponseDTO> listarPorLocador(UUID avaliadoId, Pageable pageable);

    /** Avaliações feitas por um estudante (tela "Minhas avaliações"). */
    Page<AvaliacaoResponseDTO> listarMinhasAvaliacoes(UUID avaliadorId, Pageable pageable);
}