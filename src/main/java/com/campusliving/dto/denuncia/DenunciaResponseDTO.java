package com.campusliving.dto.denuncia;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class DenunciaResponseDTO {
    private UUID id;
    private UUID denuncianteId;
    private String tipoAlvo;
    private UUID alvoId;
    private String motivo;
    private String descricao;
    private String status;
    private Integer contadorDenuncias;
    private OffsetDateTime dataCriacao;
    private String mensagem;
}