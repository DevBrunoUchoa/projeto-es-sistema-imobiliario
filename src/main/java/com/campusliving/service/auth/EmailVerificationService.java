package com.campusliving.service.auth;

import com.campusliving.model.usuario.PasswordResetToken;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.PasswordResetTokenRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public String gerarTokenVerificacao(UUID userId) {
        //Gera um token simples
        String token = UUID.randomUUID().toString();
        String tokenHash = token; // Em produção, hash com BCrypt

        PasswordResetToken verificationToken = PasswordResetToken.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiraEm(OffsetDateTime.now().plusHours(24)) // 24h para verificar
                .usado(false)
                .build();

        tokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public boolean verificarEmail(String token) {
        //Busca o token
        PasswordResetToken verificationToken = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenHash().equals(token) && !t.isUsado())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token inválido ou já utilizado"));

        //Verifica se expirou
        if (verificationToken.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        //Marca como usado
        verificationToken.setUsado(true);
        tokenRepository.save(verificationToken);

        //Ativa o usuário
        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setVerificado(true);
        userRepository.save(user);

        ///Registrar log de verificação de email
        auditLogService.registrarAcao(
                user.getId(),
                "VERIFICAR_EMAIL",
                "User",
                user.getId()
        );



        return true;
    }
}