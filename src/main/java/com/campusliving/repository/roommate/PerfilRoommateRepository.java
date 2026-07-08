package com.campusliving.repository.roommate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.roommate.PerfilRoommate;

public interface PerfilRoommateRepository extends JpaRepository<PerfilRoommate, UUID> {

    Optional<PerfilRoommate> findByUserId(UUID userId);

    // RF-33: candidatos a compatibilidade são só perfis ativos e visíveis no
    // mural, excluindo o próprio usuário (o "eu" nunca deveria aparecer na
    // lista de compatíveis consigo mesmo).
    List<PerfilRoommate> findByAtivoTrueAndPerfilVisivelTrueAndUserIdNot(UUID userId);
}
