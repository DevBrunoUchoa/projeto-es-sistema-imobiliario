package com.campusliving.dto.denuncia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DenunciaRequestDTO {

    @NotBlank(message = "Tipo de alvo é obrigatório")
    private String tipoAlvo;

    @NotNull(message = "ID do alvo é obrigatório")
    private String alvoId;

    @NotBlank(message = "Motivo é obrigatório")
    private String motivo;

    private String descricao;
}