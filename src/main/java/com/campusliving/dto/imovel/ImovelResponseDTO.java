package com.campusliving.dto.imovel;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ImovelResponseDTO {
    private UUID id;
    private UUID proprietarioId;
    private String tipo;
    private String cep;
    private String rua;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private Double latitude;
    private Double longitude;
    private Boolean ativo;
    private OffsetDateTime dataCriacao;
    private String mensagem;
}