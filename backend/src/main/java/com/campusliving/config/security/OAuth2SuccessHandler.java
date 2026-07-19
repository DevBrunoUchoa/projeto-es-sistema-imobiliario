package com.campusliving.config.security;

import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final long JWT_COOKIE_MAX_AGE = 24 * 60 * 60;
    private static final long REFRESH_COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário OAuth2 não encontrado"));

        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        response.addHeader(HttpHeaders.SET_COOKIE,
                cookie("jwt", jwt, JWT_COOKIE_MAX_AGE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookie("refresh_token", refreshToken, REFRESH_COOKIE_MAX_AGE).toString());

        //Registrar log de login via Google
        auditLogService.registrarAcao(
                user.getId(),
                "LOGIN_GOOGLE",
                "User",
                user.getId()
        );

        //Redireciona para o frontend (URL configurável via app.frontend-url / APP_FRONTEND_URL)
        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/google-login/success");
    }

    private ResponseCookie cookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")
                .build();
    }
}
