package com.campusliving.repository.notificacao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.campusliving.model.notificacao.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    Page<Notificacao> findByUserIdOrderByDataCriacaoDesc(UUID userId, Pageable pageable);

    long countByUserIdAndLidaFalse(UUID userId);

    Optional<Notificacao> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.userId = :userId AND n.lida = false")
    int marcarTodasComoLidas(@Param("userId") UUID userId);
}
