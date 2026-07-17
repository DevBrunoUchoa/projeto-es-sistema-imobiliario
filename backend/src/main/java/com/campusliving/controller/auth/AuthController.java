package com.campusliving.controller.auth;

import com.campusliving.dto.usuario.CadastroRequestDTO;
import com.campusliving.dto.usuario.CadastroResponseDTO;
import com.campusliving.dto.usuario.ForgotPasswordRequestDTO;
import com.campusliving.dto.usuario.LoginRequestDTO;
import com.campusliving.dto.usuario.LoginResponseDTO;
import com.campusliving.dto.usuario.ResetPasswordRequestDTO;
import com.campusliving.service.auth.AuthService;
import com.campusliving.service.auth.EmailVerificationService;
import com.campusliving.service.auth.PasswordResetService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final long JWT_COOKIE_MAX_AGE = 24 * 60 * 60;        // 24h
    private static final long REFRESH_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 dias

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    // RNF/SEG-03: em produção (HTTPS) os cookies de sessão são Secure. Em
    // desenvolvimento (http://localhost) fica false. Controlado por ambiente,
    // nunca fixo no código.
    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/cadastro")
    public ResponseEntity<CadastroResponseDTO> cadastrar(@Valid @RequestBody CadastroRequestDTO request) {
        CadastroResponseDTO response = authService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response
    ) {
        LoginResponseDTO loginResponse = authService.login(request);
        escreverCookiesDeSessao(response, loginResponse);
        return ResponseEntity.ok(semTokensNoCorpo(loginResponse));
    }

    // RNF/SEG-02: troca o refresh token (validade 7d) por um novo par de
    // tokens sem exigir novo login. Lê o refresh token do cookie HttpOnly.
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        LoginResponseDTO loginResponse = authService.refresh(refreshToken);
        escreverCookiesDeSessao(response, loginResponse);
        return ResponseEntity.ok(semTokensNoCorpo(loginResponse));
    }


    @GetMapping("/me")
    public ResponseEntity<LoginResponseDTO> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.usuarioAtual(authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieDeSessao("jwt", "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieDeSessao("refresh_token", "", 0).toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verificar-email/{token}")
    public ResponseEntity<String> verificarEmail(@PathVariable String token) {
        boolean verificado = emailVerificationService.verificarEmail(token);
        if (verificado) {
            return ResponseEntity.ok("Email verificado com sucesso!");
        }
        return ResponseEntity.badRequest().body("Falha na verificação do email");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        // Sempre responde a mesma mensagem, exista o e-mail ou não (evita
        // enumeração de contas). O link vai por e-mail, nunca no corpo.
        passwordResetService.gerarTokenReset(request.getEmail());
        return ResponseEntity.ok("Se o e-mail estiver cadastrado, enviaremos um link de redefinição.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetarSenha(request.getToken(), request.getNovaSenha());
        return ResponseEntity.ok("Senha resetada com sucesso!");
    }

    // --- helpers -----------------------------------------------------------

    private void escreverCookiesDeSessao(HttpServletResponse response, LoginResponseDTO login) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieDeSessao("jwt", login.getJwtToken(), JWT_COOKIE_MAX_AGE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                cookieDeSessao("refresh_token", login.getRefreshToken(), REFRESH_COOKIE_MAX_AGE).toString());
    }

    private ResponseCookie cookieDeSessao(String nome, String valor, long maxAgeSegundos) {
        return ResponseCookie.from(nome, valor)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAgeSegundos)
                .sameSite("Lax")
                .build();
    }

    private LoginResponseDTO semTokensNoCorpo(LoginResponseDTO login) {
        login.setJwtToken(null);
        login.setRefreshToken(null);
        return login;
    }
}
