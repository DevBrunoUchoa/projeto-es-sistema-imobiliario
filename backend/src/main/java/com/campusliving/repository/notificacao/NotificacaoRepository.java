package com.campusliving.repository.notificacao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.notificacao.Notificacao;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {
}
