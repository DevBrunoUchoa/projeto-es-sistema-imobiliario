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

import com.campusliving.model.usuario.PasswordResetToken;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.PasswordResetTokenRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;

/** Testes unitários da confirmação de e-mail de contas recém-cadastradas. */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;

    private EmailVerificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationService(tokenRepository, userRepository, auditLogService);
    }

    @Test
    void gerarTokenVerificacao_devePersistirTokenAtivoComExpiracaoDeUmDia() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime antes = OffsetDateTime.now();

        String tokenGerado = service.gerarTokenVerificacao(userId);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken token = tokenCaptor.getValue();
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getTokenHash()).isEqualTo(tokenGerado);
        assertThat(token.isUsado()).isFalse();
        assertThat(token.getExpiraEm()).isAfter(antes.plusHours(23).plusMinutes(59));
    }

    @Test
    void verificarEmail_comTokenValido_deveAtivarUsuarioConsumirTokenEAuditar() {
        UUID userId = UUID.randomUUID();
        PasswordResetToken token = token(userId, "confirmar", OffsetDateTime.now().plusHours(24), false);
        User user = User.builder().id(userId).verificado(false).build();
        when(tokenRepository.findAll()).thenReturn(List.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        boolean verificado = service.verificarEmail("confirmar");

        assertThat(verificado).isTrue();
        assertThat(token.isUsado()).isTrue();
        assertThat(user.isVerificado()).isTrue();
        verify(tokenRepository).save(token);
        verify(userRepository).save(user);
        verify(auditLogService).registrarAcao(userId, "VERIFICAR_EMAIL", "User", userId);
    }

    @Test
    void verificarEmail_comTokenInvalido_naoDevePersistirAlteracoes() {
        when(tokenRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> service.verificarEmail("invalido"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Token inválido ou já utilizado");

        verify(tokenRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void verificarEmail_comTokenExpirado_naoDeveAtivarUsuario() {
        PasswordResetToken token = token(UUID.randomUUID(), "expirado", OffsetDateTime.now().minusSeconds(1), false);
        when(tokenRepository.findAll()).thenReturn(List.of(token));

        assertThatThrownBy(() -> service.verificarEmail("expirado"))
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
