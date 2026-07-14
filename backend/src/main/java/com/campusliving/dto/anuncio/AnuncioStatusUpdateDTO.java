package com.campusliving.dto.anuncio;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnuncioStatusUpdateDTO {

    @NotBlank(message = "Status é obrigatório")
    private String status; // ATIVO, INATIVO, ALUGADO
}