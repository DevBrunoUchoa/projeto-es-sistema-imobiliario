package com.campusliving.service.notificacao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.dto.notificacao.NotificacaoResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.notificacao.Notificacao;
import com.campusliving.repository.notificacao.NotificacaoRepository;

/**
 * Recuperação e leitura de notificações in-app (RF-39). Cada usuário só
 * enxerga e altera as próprias notificações.
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public NotificacaoService(NotificacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<NotificacaoResponseDTO> listar(UUID userId, Pageable pageable) {
        exigirAutenticado(userId);
        return repository.findByUserIdOrderByDataCriacaoDesc(userId, pageable)
                .map(NotificacaoResponseDTO::of);
    }

    @Transactional(readOnly = true)
    public long contarNaoLidas(UUID userId) {
        exigirAutenticado(userId);
        return repository.countByUserIdAndLidaFalse(userId);
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(UUID id, UUID userId) {
        exigirAutenticado(userId);
        Notificacao notificacao = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ProjectException("Notificação não encontrada", HttpStatus.NOT_FOUND));
        notificacao.setLida(true);
        repository.save(notificacao);
        return NotificacaoResponseDTO.of(notificacao);
    }

    @Transactional
    public int marcarTodasComoLidas(UUID userId) {
        exigirAutenticado(userId);
        return repository.marcarTodasComoLidas(userId);
    }

    private void exigirAutenticado(UUID userId) {
        if (userId == null) {
            throw new AcessoNegadoException();
        }
    }
}
