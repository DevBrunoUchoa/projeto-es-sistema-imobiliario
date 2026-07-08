package com.campusliving.dto.usuario;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private String mensagem;
    private String jwtToken;    
    private String refreshToken;  
}