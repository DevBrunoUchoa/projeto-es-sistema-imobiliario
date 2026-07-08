package com.campusliving.service.auth;

import com.campusliving.config.security.JwtService;
import com.campusliving.dto.usuario.CadastroRequestDTO;
import com.campusliving.dto.usuario.CadastroResponseDTO;
import com.campusliving.dto.usuario.LoginRequestDTO;
import com.campusliving.dto.usuario.LoginResponseDTO;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public CadastroResponseDTO cadastrar(CadastroRequestDTO request) {
        // Verifica se email já existe
        List<User> existingUsers = userRepository.findByEmail(request.getEmail());
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (!Boolean.TRUE.equals(request.getAceiteLgpd())) {
            throw new RuntimeException("Aceite do LGPD é obrigatório");
        }

        String role = request.getRole() != null ? request.getRole() : "ESTUDANTE";

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .aceiteLgpd(true)
                .emailVerificado(false)
                .build();

        User savedUser = userRepository.save(user);

        return CadastroResponseDTO.builder()
                .id(savedUser.getId()) // UUID
                .nome(savedUser.getNome())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .emailVerificado(savedUser.getEmailVerificado())
                .mensagem("Usuário cadastrado com sucesso! Verifique seu email para ativar a conta.")
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        List<User> users = userRepository.findByEmail(request.getEmail());
        if (users.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }
        User user = users.get(0);

        if (!passwordEncoder.matches(request.getSenha(), user.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponseDTO.builder()
                .id(user.getId()) // UUID
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole())
                .mensagem("Login realizado com sucesso!")
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}