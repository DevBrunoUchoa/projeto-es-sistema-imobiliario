package com.campusliving.dto.interacao;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corpo do POST /interesses (RF-28). O id do estudante NÃO vem no corpo:
// vem do requerente autenticado (ver comentário sobre X-User-Id no
// ContatoController) — do contrário qualquer um poderia registrar interesse
// em nome de outro usuário.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteresseRequestDTO {

    @JsonProperty("adId")
    @NotNull(message = "adId e obrigatorio")
    private UUID adId;

    @JsonProperty("mensagem")
    @NotBlank(message = "mensagem e obrigatoria")
    private String mensagem;

    // Opcional (default false): estudante consente em liberar o próprio
    // contato (e-mail/telefone) ao locador do anúncio. RNF/LEG-03.
    @JsonProperty("liberarContato")
    private boolean liberarContato;
}