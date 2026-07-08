package com.campusliving.repository.usuario;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.usuario.VerificacaoLocador;

public interface VerificacaoLocadorRepository extends JpaRepository<VerificacaoLocador, UUID> {

    List<VerificacaoLocador> findByUserIdAndStatus(UUID userId, String status);
}
