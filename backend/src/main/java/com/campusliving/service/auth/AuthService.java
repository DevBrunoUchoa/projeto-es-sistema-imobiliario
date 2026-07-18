package com.campusliving.service.auth;

import com.campusliving.config.security.JwtService;
import com.campusliving.dto.usuario.CadastroRequestDTO;
import com.campusliving.dto.usuario.CadastroResponseDTO;
import com.campusliving.dto.usuario.LoginRequestDTO;
import com.campusliving.dto.usuario.LoginResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final AuditLogService auditLogService;
    private final com.campusliving.service.email.EmailService emailService;

    @Transactional
    public CadastroResponseDTO cadastrar(CadastroRequestDTO request) {
        //Verifica se email já existe
        List<User> existingUsers = userRepository.findByEmail(request.getEmail());
        if (!existingUsers.isEmpty()) {
            throw new ProjectException("Email já cadastrado", HttpStatus.CONFLICT);
        }

        if (!Boolean.TRUE.equals(request.getAceiteLgpd())) {
            throw new ProjectException("Aceite do LGPD é obrigatório", HttpStatus.BAD_REQUEST);
        }

        //Define role padrão (ESTUDANTE)
        User.Tipo tipo = User.Tipo.ESTUDANTE;
        if (request.getRole() != null) {
            User.Tipo tipoSolicitado;
            try {
                tipoSolicitado = User.Tipo.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ProjectException("Role inválida. Valores permitidos: ESTUDANTE, LOCADOR, MISTO", HttpStatus.BAD_REQUEST);
            }
            // RNF/SEG: ADMIN nunca pode ser auto-atribuído no cadastro público
            // (endpoint é permitAll) — sem isso, qualquer um vira admin da
            // plataforma só passando "role":"ADMIN" no corpo da requisição.
            // Promoção a admin é feita manualmente, fora deste endpoint.
            if (tipoSolicitado == User.Tipo.ADMIN) {
                throw new ProjectException("Role inválida. Valores permitidos: ESTUDANTE, LOCADOR, MISTO", HttpStatus.BAD_REQUEST);
            }
            tipo = tipoSolicitado;
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .tipoConta(tipo)
                .aceiteLgpd(true)
                .verificado(false)
                .ativo(true)
                .build();

        User savedUser = userRepository.save(user);

        //Registrar log de cadastro
        auditLogService.registrarAcao(
                savedUser.getId(),
                "CADASTRO_USUARIO",
                "User",
                savedUser.getId()
        );

        //Gera token de verificação e envia por e-mail (RF-05).
        //O token NÃO é devolvido no corpo da resposta (evita vazamento).
        String token = emailVerificationService.gerarTokenVerificacao(savedUser.getId());
        emailService.enviarVerificacaoEmail(savedUser.getEmail(), savedUser.getNome(), token);

        //Retorna a resposta
        return CadastroResponseDTO.builder()
                .id(savedUser.getId())
                .nome(savedUser.getNome())
                .email(savedUser.getEmail())
                .role(savedUser.getTipoConta().name())
                .emailVerificado(savedUser.isVerificado())
                .mensagem("Usuário cadastrado com sucesso! Verifique seu e-mail para ativar a conta.")
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        List<User> users = userRepository.findByEmail(request.getEmail());
        if (users.isEmpty()) {
            throw new ProjectException("Usuário não encontrado", HttpStatus.UNAUTHORIZED);
        }
        User user = users.get(0);

        if (!user.isAtivo()) {
            throw new ProjectException("Conta desativada", HttpStatus.UNAUTHORIZED);
        }

        if (!passwordEncoder.matches(request.getSenha(), user.getSenhaHash())) {
            throw new ProjectException("Senha inválida", HttpStatus.UNAUTHORIZED);
        }

        //Registrar log de login
        auditLogService.registrarAcao(
                user.getId(),
                "LOGIN_USUARIO",
                "User",
                user.getId()
        );

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponseDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getTipoConta().name())
                .mensagem("Login realizado com sucesso!")
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // RNF/SEG-02: renova o par de tokens a partir de um refresh token válido
    // (validade máxima 7 dias). Falhas de token retornam 401, não 500.
    public LoginResponseDTO refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ProjectException("Refresh token ausente", HttpStatus.UNAUTHORIZED);
        }

        final String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new ProjectException("Refresh token inválido", HttpStatus.UNAUTHORIZED);
        }

        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new ProjectException("Refresh token inválido", HttpStatus.UNAUTHORIZED);
        }
        User user = users.get(0);

        if (!user.isAtivo() || !jwtService.isTokenValid(refreshToken, user)) {
            throw new ProjectException("Refresh token expirado ou revogado", HttpStatus.UNAUTHORIZED);
        }

        return LoginResponseDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getTipoConta().name())
                .mensagem("Token renovado com sucesso!")
                .jwtToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .build();
    }
}