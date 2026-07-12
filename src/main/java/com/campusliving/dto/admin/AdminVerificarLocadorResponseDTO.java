package com.campusliving.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminVerificarLocadorResponseDTO {
    private UUID id;
    private String nome;
    private String email;
    private String tipoConta;
    private Boolean verificado;
    private String mensagem;
}