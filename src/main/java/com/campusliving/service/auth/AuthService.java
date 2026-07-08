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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public CadastroResponseDTO cadastrar(CadastroRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (!Boolean.TRUE.equals(request.getAceiteLgpd())) {
            throw new RuntimeException("Aceite do LGPD é obrigatório");
        }

        String role = request.getRole() != null ? request.getRole() : "ESTUDANTE";

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .aceiteLgpd(true)
                .emailVerificado(false)
                .build();

        User savedUser = userRepository.save(user);

        return CadastroResponseDTO.builder()
                .id(savedUser.getId())
                .nome(savedUser.getNome())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .emailVerificado(savedUser.getEmailVerificado())
                .mensagem("Usuário cadastrado com sucesso! Verifique seu email para ativar a conta.")
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponseDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole())
                .mensagem("Login realizado com sucesso!")
                .jwtToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }
}