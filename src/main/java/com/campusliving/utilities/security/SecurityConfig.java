package com.campusliving.utilities.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // RNF/SEG-01: custo mínimo de iteração 12 (o padrão do BCryptPasswordEncoder
    // sem argumento é 10, que não atende ao requisito).
    private static final int BCRYPT_STRENGTH = 12;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCRYPT_STRENGTH);
    }

    // IMPORTANTE: assim que "spring-boot-starter-security" está no classpath,
    // o Spring Boot ativa por padrão um auto-config que exige HTTP Basic
    // (usuário "user" + senha aleatória impressa no log) em TODA rota — sem
    // isso aqui, nem o cadastro de usuário funcionaria. Como a autenticação
    // "de verdade" (JWT, RF de login) é escopo do T5.3 e ainda não existe,
    // este filter chain fica propositalmente permissivo (libera tudo, CSRF
    // desabilitado por ser API stateless) só para não travar o resto do time.
    // TODO(T5.3): substituir por regras reais (rotas públicas x autenticadas)
    // quando o login com JWT for implementado.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
