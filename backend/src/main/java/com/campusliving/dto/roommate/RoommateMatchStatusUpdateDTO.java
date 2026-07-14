package com.campusliving.dto.roommate;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Corpo do PATCH /roommates/match/:id (RF-34/RF-35). Só ACEITO/RECUSADO são
// aceitos aqui — PENDENTE/CANCELADO não fazem sentido como resposta do
// destinatário (o serviço valida isso, ver StatusMatchInvalidoException).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoommateMatchStatusUpdateDTO {

    @JsonProperty("status")
    @NotNull(message = "status e obrigatorio")
    private String status;
}
