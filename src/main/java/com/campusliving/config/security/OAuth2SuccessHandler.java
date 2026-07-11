package com.campusliving.config.security;

import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        //Registrar log de login via Google
        auditLogService.registrarAcao(
                user.getId(),
                "LOGIN_GOOGLE",
                "User",
                user.getId()
        );

        //Redireciona para o frontend
        response.sendRedirect("http://localhost:3000/auth/success");
    }
}