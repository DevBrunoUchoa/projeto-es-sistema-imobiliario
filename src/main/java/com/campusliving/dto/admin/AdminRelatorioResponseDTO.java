package com.campusliving.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminRelatorioResponseDTO {
    private Long totalUsuarios;
    private Long totalAnunciosAtivos;
    private Long totalDenunciasPendentes;
    private Long totalDenunciasResolvidas;
    private Long totalLocadoresVerificados;
    private String mensagem;
}