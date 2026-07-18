package com.campusliving.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campusliving.config.security.JwtService;
import com.campusliving.dto.usuario.CadastroRequestDTO;
import com.campusliving.dto.usuario.CadastroResponseDTO;
import com.campusliving.dto.usuario.LoginRequestDTO;
import com.campusliving.dto.usuario.LoginResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import com.campusliving.service.email.EmailService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private EmailService emailService;

    private AuthService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                emailVerificationService,
                auditLogService,
                emailService
        );

        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .nome("Ana Estudante")
                .email("ana@ufcg.edu.br")
                .senhaHash("senha-hash")
                .tipoConta(User.Tipo.ESTUDANTE)
                .aceiteLgpd(true)
                .verificado(true)
                .ativo(true)
                .build();
    }

    // --- cadastrar -------------------------------------------------------

    @Test
    void cadastrar_quandoDadosValidosSemRole_devePersistirEstudanteEEnviarVerificacao() {
        CadastroRequestDTO request = cadastroRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of());
        when(passwordEncoder.encode(request.getSenha())).thenReturn("senha-criptografada");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User novoUsuario = invocation.getArgument(0);
            novoUsuario.setId(userId);
            return novoUsuario;
        });
        when(emailVerificationService.gerarTokenVerificacao(userId)).thenReturn("token-de-verificacao");

        CadastroResponseDTO response = service.cadastrar(request);

        ArgumentCaptor<User> usuarioCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(usuarioCaptor.capture());
        User usuarioSalvo = usuarioCaptor.getValue();
        assertThat(usuarioSalvo.getNome()).isEqualTo(request.getNome());
        assertThat(usuarioSalvo.getEmail()).isEqualTo(request.getEmail());
        assertThat(usuarioSalvo.getSenhaHash()).isEqualTo("senha-criptografada");
        assertThat(usuarioSalvo.getTipoConta()).isEqualTo(User.Tipo.ESTUDANTE);
        assertThat(usuarioSalvo.isAceiteLgpd()).isTrue();
        assertThat(usuarioSalvo.isVerificado()).isFalse();
        assertThat(usuarioSalvo.isAtivo()).isTrue();

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getNome()).isEqualTo(request.getNome());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo(User.Tipo.ESTUDANTE.name());
        assertThat(response.getEmailVerificado()).isFalse();
        assertThat(response.getMensagem()).contains("Verifique seu e-mail");
        verify(auditLogService).registrarAcao(userId, "CADASTRO_USUARIO", "User", userId);
        verify(emailVerificationService).gerarTokenVerificacao(userId);
        verify(emailService).enviarVerificacaoEmail(
                request.getEmail(), request.getNome(), "token-de-verificacao");
    }

    @Test
    void cadastrar_quandoRoleValidaInformada_deveUsarRoleInformadaSemDiferenciarMaiusculas() {
        CadastroRequestDTO request = cadastroRequest();
        request.setRole("locador");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of());
        when(passwordEncoder.encode(request.getSenha())).thenReturn("senha-criptografada");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User novoUsuario = invocation.getArgument(0);
            novoUsuario.setId(userId);
            return novoUsuario;
        });
        when(emailVerificationService.gerarTokenVerificacao(userId)).thenReturn("token");

        CadastroResponseDTO response = service.cadastrar(request);

        ArgumentCaptor<User> usuarioCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(usuarioCaptor.capture());
        assertThat(usuarioCaptor.getValue().getTipoConta()).isEqualTo(User.Tipo.LOCADOR);
        assertThat(response.getRole()).isEqualTo(User.Tipo.LOCADOR.name());
    }

    @Test
    void cadastrar_quandoEmailJaExiste_deveRejeitarSemPersistir() {
        CadastroRequestDTO request = cadastroRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of(user));

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email já cadastrado");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailVerificationService, auditLogService, emailService);
    }

    @Test
    void cadastrar_quandoLgpdNaoFoiAceita_deveRejeitarSemPersistir() {
        CadastroRequestDTO request = cadastroRequest();
        request.setAceiteLgpd(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of());

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Aceite do LGPD é obrigatório");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailVerificationService, auditLogService, emailService);
    }

    @Test
    void cadastrar_quandoRoleInvalida_deveRejeitarSemPersistir() {
        CadastroRequestDTO request = cadastroRequest();
        request.setRole("VISITANTE");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of());

        assertThatThrownBy(() -> service.cadastrar(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role inválida. Valores permitidos: ESTUDANTE, LOCADOR, MISTO, ADMIN");

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, emailVerificationService, auditLogService, emailService);
    }

    // --- login -----------------------------------------------------------

    @Test
    void login_quandoCredenciaisValidas_deveRetornarParDeTokensERegistrarAuditoria() {
        LoginRequestDTO request = loginRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of(user));
        when(passwordEncoder.matches(request.getSenha(), user.getSenhaHash())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        LoginResponseDTO response = service.login(request);

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getNome()).isEqualTo(user.getNome());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRole()).isEqualTo(User.Tipo.ESTUDANTE.name());
        assertThat(response.getMensagem()).isEqualTo("Login realizado com sucesso!");
        assertThat(response.getJwtToken()).isEqualTo("jwt-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(passwordEncoder).matches(request.getSenha(), user.getSenhaHash());
        verify(auditLogService).registrarAcao(userId, "LOGIN_USUARIO", "User", userId);
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void login_quandoUsuarioNaoExiste_deveRejeitarSemConsultarSenhaOuGerarTokens() {
        LoginRequestDTO request = loginRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of());

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");

        verifyNoInteractions(passwordEncoder, jwtService, auditLogService);
    }

    @Test
    void login_quandoContaEstaDesativada_deveRejeitarAntesDeValidarSenha() {
        LoginRequestDTO request = loginRequest();
        user.setAtivo(false);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of(user));

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Conta desativada");

        verifyNoInteractions(passwordEncoder, jwtService, auditLogService);
    }

    @Test
    void login_quandoSenhaEstaInvalida_deveRejeitarSemGerarTokensOuAuditoria() {
        LoginRequestDTO request = loginRequest();
        when(userRepository.findByEmail(request.getEmail())).thenReturn(List.of(user));
        when(passwordEncoder.matches(request.getSenha(), user.getSenhaHash())).thenReturn(false);

        assertThatThrownBy(() -> service.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Senha inválida");

        verify(passwordEncoder).matches(request.getSenha(), user.getSenhaHash());
        verifyNoInteractions(jwtService, auditLogService);
    }

    // --- refresh ---------------------------------------------------------

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void refresh_quandoTokenAusenteOuEmBranco_deveRetornarNaoAutorizado(String refreshToken) {
        ProjectException exception = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> service.refresh(refreshToken), ProjectException.class);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception).hasMessage("Refresh token ausente");
        verifyNoInteractions(userRepository, jwtService, passwordEncoder, auditLogService);
    }

    @Test
    void refresh_quandoTokenNaoPodeSerLido_deveRetornarNaoAutorizado() {
        when(jwtService.extractUsername("token-invalido"))
                .thenThrow(new IllegalArgumentException("assinatura inválida"));

        ProjectException exception = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> service.refresh("token-invalido"), ProjectException.class);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception).hasMessage("Refresh token inválido");
        verifyNoInteractions(userRepository);
    }

    @Test
    void refresh_quandoUsuarioDoTokenNaoExiste_deveRetornarNaoAutorizado() {
        when(jwtService.extractUsername("token-valido")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(List.of());

        ProjectException exception = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> service.refresh("token-valido"), ProjectException.class);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception).hasMessage("Refresh token inválido");
        verify(jwtService, never()).isTokenValid("token-valido", user);
    }

    @Test
    void refresh_quandoContaFoiDesativada_deveRetornarNaoAutorizadoSemValidarToken() {
        user.setAtivo(false);
        when(jwtService.extractUsername("token-valido")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(List.of(user));

        ProjectException exception = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> service.refresh("token-valido"), ProjectException.class);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception).hasMessage("Refresh token expirado ou revogado");
        verify(jwtService, never()).isTokenValid("token-valido", user);
    }

    @Test
    void refresh_quandoTokenEstaExpiradoOuRevogado_deveRetornarNaoAutorizado() {
        when(jwtService.extractUsername("token-expirado")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(List.of(user));
        when(jwtService.isTokenValid("token-expirado", user)).thenReturn(false);

        ProjectException exception = org.assertj.core.api.Assertions.catchThrowableOfType(
                () -> service.refresh("token-expirado"), ProjectException.class);

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception).hasMessage("Refresh token expirado ou revogado");
    }

    @Test
    void refresh_quandoTokenValidoDeContaAtiva_deveGerarNovoParDeTokens() {
        when(jwtService.extractUsername("refresh-atual")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(List.of(user));
        when(jwtService.isTokenValid("refresh-atual", user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("novo-jwt");
        when(jwtService.generateRefreshToken(user)).thenReturn("novo-refresh");

        LoginResponseDTO response = service.refresh("refresh-atual");

        assertThat(response.getId()).isEqualTo(userId);
        assertThat(response.getNome()).isEqualTo(user.getNome());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getRole()).isEqualTo(User.Tipo.ESTUDANTE.name());
        assertThat(response.getMensagem()).isEqualTo("Token renovado com sucesso!");
        assertThat(response.getJwtToken()).isEqualTo("novo-jwt");
        assertThat(response.getRefreshToken()).isEqualTo("novo-refresh");
        verify(jwtService).isTokenValid("refresh-atual", user);
        verify(jwtService).generateToken(user);
        verify(jwtService).generateRefreshToken(user);
        verifyNoInteractions(auditLogService);
    }

    private CadastroRequestDTO cadastroRequest() {
        CadastroRequestDTO request = new CadastroRequestDTO();
        request.setNome("Ana Estudante");
        request.setEmail("ana@ufcg.edu.br");
        request.setSenha("Senha123");
        request.setAceiteLgpd(true);
        return request;
    }

    private LoginRequestDTO loginRequest() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(user.getEmail());
        request.setSenha("Senha123");
        return request;
    }
}
