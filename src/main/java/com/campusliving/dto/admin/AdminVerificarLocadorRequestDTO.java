package com.campusliving.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminVerificarLocadorRequestDTO {

    @NotNull(message = "Campo 'verificado' é obrigatório")
    private Boolean verificado;
}