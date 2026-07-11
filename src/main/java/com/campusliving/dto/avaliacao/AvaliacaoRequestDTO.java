package com.campusliving.dto.avaliacao;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corpo do POST /avaliacoes (RF-29). O id do avaliador NÃO vem no corpo: vem
// do requerente autenticado (mesmo padrão do X-User-Id documentado em
// UserController/ContatoController) — do contrário qualquer um poderia
// publicar uma avaliação em nome de outro usuário.
//
// "contato_previo" também não é um campo deste DTO de propósito: o RF-29 diz
// que "avaliador deve ter tido vínculo com o anunciante", e essa é uma
// verificação que o SERVIDOR faz (via ContatoRepository), não uma alegação
// que o cliente possa simplesmente declarar como true.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoRequestDTO {

    @JsonProperty("adId")
    @NotNull(message = "adId e obrigatorio")
    private UUID adId;

    @JsonProperty("nota")
    @NotNull(message = "nota e obrigatoria")
    @Min(value = 1, message = "A nota minima e 1")
    @Max(value = 5, message = "A nota maxima e 5")
    private Integer nota;

    @JsonProperty("comentario")
    @NotBlank(message = "comentario e obrigatorio")
    @Size(max = 1000, message = "comentario deve ter no maximo 1000 caracteres")
    private String comentario;
}