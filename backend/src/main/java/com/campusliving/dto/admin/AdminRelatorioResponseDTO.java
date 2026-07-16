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

    // RF-43: métricas do período selecionado (7/30/90 dias).
    private Integer periodoDias;
    private Long novosCadastrosPeriodo;
    private Long anunciosPublicadosPeriodo;
    private Long denunciasPeriodo;

    private String mensagem;
}