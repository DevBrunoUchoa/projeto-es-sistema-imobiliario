package com.campusliving.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campusliving.model.usuario.PasswordResetToken;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.PasswordResetTokenRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import com.campusliving.service.email.EmailService;

/** Testes unitários do fluxo de recuperação de senha (RF-04). */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                tokenRepository, userRepository, passwordEncoder, auditLogService, emailService);
    }

    @Test
    void gerarTokenReset_quandoEmailExiste_devePersistirTokenAuditarEEnviarEmail() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).nome("Ana").email("ana@ufcg.edu.br").build();
        OffsetDateTime antes = OffsetDateTime.now();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(List.of(user));

        service.gerarTokenReset(user.getEmail());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken token = tokenCaptor.getValue();
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getTokenHash()).isNotBlank();
        assertThat(token.isUsado()).isFalse();
        assertThat(token.getExpiraEm()).isAfter(antes.plusMinutes(59));
        verify(auditLogService).registrarAcao(userId, "SOLICITAR_RESET_SENHA", "User", userId);
        verify(emailService).enviarResetSenha(user.getEmail(), user.getNome(), token.getTokenHash());
    }

    @Test
    void gerarTokenReset_quandoEmailNaoExiste_deveEncerrarSemEfeitosColaterais() {
        when(userRepository.findByEmail("inexistente@ufcg.edu.br")).thenReturn(List.of());

        service.gerarTokenReset("inexistente@ufcg.edu.br");

        verify(tokenRepository, never()).save(any());
        verify(auditLogService, never()).registrarAcao(any(), any(), any(), any());
        verify(emailService, never()).enviarResetSenha(any(), any(), any());
    }

    @Test
    void resetarSenha_comTokenValido_deveMarcarComoUsadoEAtualizarSenha() {
        UUID userId = UUID.randomUUID();
        PasswordResetToken token = token(userId, "token-valido", OffsetDateTime.now().plusHours(1), false);
        User user = User.builder().id(userId).senhaHash("senha-antiga").build();
        when(tokenRepository.findAll()).thenReturn(List.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nova-senha")).thenReturn("senha-codificada");

        service.resetarSenha("token-valido", "nova-senha");

        assertThat(token.isUsado()).isTrue();
        assertThat(user.getSenhaHash()).isEqualTo("senha-codificada");
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
        verify(auditLogService).registrarAcao(userId, "RESETAR_SENHA", "User", userId);
    }

    @Test
    void resetarSenha_comTokenInvalido_naoDeveAlterarSenhaNemPersistir() {
        when(tokenRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.resetarSenha("invalido", "nova-senha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token inválido ou já utilizado");

        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void resetarSenha_comTokenExpirado_naoDeveConsumirToken() {
        PasswordResetToken token = token(UUID.randomUUID(), "expirado", OffsetDateTime.now().minusSeconds(1), false);
        when(tokenRepository.findAll()).thenReturn(List.of(token));

        assertThatThrownBy(() -> service.resetarSenha("expirado", "nova-senha"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token expirado");

        assertThat(token.isUsado()).isFalse();
        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    private PasswordResetToken token(UUID userId, String hash, OffsetDateTime expiraEm, boolean usado) {
        return PasswordResetToken.builder()
                .userId(userId)
                .tokenHash(hash)
                .expiraEm(expiraEm)
                .usado(usado)
                .build();
    }
}
