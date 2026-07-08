package com.campusliving.dto.usuario;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CadastroResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private String role;
    private Boolean emailVerificado;
    private String mensagem;
}