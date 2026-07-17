package com.campusliving.service.auth;

import com.campusliving.model.usuario.PasswordResetToken;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.PasswordResetTokenRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final com.campusliving.service.email.EmailService emailService;

    @Transactional
    public void gerarTokenReset(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            // Não revela se o e-mail existe (evita enumeração de contas):
            // apenas encerra silenciosamente. O controller responde a mesma
            // mensagem genérica em ambos os casos.
            return;
        }

        User user = users.get(0);

        //Gerar token
        String token = UUID.randomUUID().toString();
        String tokenHash = token; //hash com BCrypt

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(user.getId())
                .tokenHash(tokenHash)
                .expiraEm(OffsetDateTime.now().plusHours(1)) // 1h de expiração - RF-04
                .usado(false)
                .build();

        tokenRepository.save(resetToken);

        auditLogService.registrarAcao(
                user.getId(),
                "SOLICITAR_RESET_SENHA",
                "User",
                user.getId()
        );

        // RF-04: envia o link de redefinição por e-mail (não devolve o token
        // na resposta HTTP).
        emailService.enviarResetSenha(user.getEmail(), user.getNome(), token);
    }

    @Transactional
    public void resetarSenha(String token, String novaSenha) {
        // Buscar token válido
        PasswordResetToken resetToken = tokenRepository.findAll().stream()
                .filter(t -> t.getTokenHash().equals(token) && !t.isUsado())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Token inválido ou já utilizado"));

        //Verificar expiração (1h - RF-04)
        if (resetToken.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Token expirado");
        }

        //Marcar token como usado
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        //Buscar usuário
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        //Atualizar senha
        user.setSenhaHash(passwordEncoder.encode(novaSenha));
        userRepository.save(user);

        auditLogService.registrarAcao(
                user.getId(),
                "RESETAR_SENHA",
                "User",
                user.getId()
        );
    }
}