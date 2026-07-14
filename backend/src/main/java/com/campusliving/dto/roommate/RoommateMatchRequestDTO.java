package com.campusliving.dto.roommate;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corpo do POST /roommates/match (RF-34). O solicitante vem do requerente
// autenticado (X-User-Id), não do corpo — mesma lógica já usada em
// InteresseRequestDTO (T5.4.5).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateMatchRequestDTO {

    @JsonProperty("destinatarioId")
    @NotNull(message = "destinatarioId e obrigatorio")
    private UUID destinatarioId;

    @JsonProperty("mensagemInicial")
    private String mensagemInicial;
}
