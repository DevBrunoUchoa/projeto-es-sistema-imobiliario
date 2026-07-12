package com.campusliving.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AdminUsuarioResponseDTO {
    private UUID id;
    private String nome;
    private String email;
    private String tipoConta;
    private Boolean verificado;
    private Boolean ativo;
    private OffsetDateTime dataCriacao;
}