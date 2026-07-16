package com.campusliving.dto.imovel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ImovelRequestDTO {

    @NotNull(message = "Tipo do imóvel é obrigatório")
    private String tipo; //APARTAMENTO, QUARTO, FLAT, PENSIONATO

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
    private String cep;

    @NotBlank(message = "Rua é obrigatória")
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    private String numero;

    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Pattern(regexp = "[A-Z]{2}", message = "Estado deve ter 2 letras maiúsculas")
    private String estado;

    // RF-16: opcionais. Se omitidas, o sistema geocodifica o endereço
    // (Nominatim). Se informadas, têm prioridade sobre a geocodificação.
    private Double latitude;

    private Double longitude;
}