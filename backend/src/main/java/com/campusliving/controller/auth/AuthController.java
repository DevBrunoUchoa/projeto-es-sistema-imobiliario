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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

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

        //Criar cookie HttpOnly para o JWT (24h)
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", loginResponse.getJwtToken())
                .httpOnly(true)
                .secure(false) //true em produção (HTTPS)
                .path("/")
                .maxAge(24 * 60 * 60) //24h em segundos
                .sameSite("Lax")
                .build();

        //Criar cookie HttpOnly para o Refresh Token (7d)
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) //true em produção (HTTPS)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) //7 dias em segundos
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        //Remove os tokens do body (não expor)
        loginResponse.setJwtToken(null);
        loginResponse.setRefreshToken(null);

        return ResponseEntity.ok(loginResponse);
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
        String token = passwordResetService.gerarTokenReset(request.getEmail());
        return ResponseEntity.ok("Token de reset gerado com sucesso! Token: " + token);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        passwordResetService.resetarSenha(request.getToken(), request.getNovaSenha());
        return ResponseEntity.ok("Senha resetada com sucesso!");
    }
}