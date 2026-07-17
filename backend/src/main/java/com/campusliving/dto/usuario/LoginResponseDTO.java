package com.campusliving.dto.usuario;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class LoginResponseDTO {
    private UUID id;
    private String nome;
    private String email;
    private String role;
    private String fotoUrl;
    private String mensagem;
    private String jwtToken;    
    private String refreshToken;  
}