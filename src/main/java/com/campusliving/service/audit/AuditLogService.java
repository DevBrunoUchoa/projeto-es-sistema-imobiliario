package com.campusliving.service.audit;

import com.campusliving.model.auditoria.AuditLog;
import com.campusliving.repository.auditoria.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void registrarAcao(UUID userId, String acao, String entidade, UUID entidadeId, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(log);
    }

    @Transactional
    public void registrarAcao(UUID userId, String acao, String entidade, UUID entidadeId) {
        registrarAcao(userId, acao, entidade, entidadeId, null);
    }

    @Transactional
    public void registrarAcao(UUID userId, String acao, String entidade) {
        registrarAcao(userId, acao, entidade, null, null);
    }
}