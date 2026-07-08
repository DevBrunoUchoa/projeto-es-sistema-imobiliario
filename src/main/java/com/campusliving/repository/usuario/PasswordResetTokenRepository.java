package com.campusliving.repository.usuario;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.campusliving.model.usuario.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
}
