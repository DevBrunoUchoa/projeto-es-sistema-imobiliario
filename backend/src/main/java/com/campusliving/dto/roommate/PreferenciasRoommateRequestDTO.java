package com.campusliving.dto.roommate;

import java.time.LocalTime;

import com.campusliving.model.roommate.PerfilRoommate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo do PUT /usuarios/:id/preferencias-roommate (RF-10/RF-32).
 *
 * <p>Atualização parcial (mesmo padrão do T5.4): só os campos não-nulos
 * enviados são alterados. Se o usuário ainda não tem
 * {@code roommate_profiles}, esse endpoint cria um registro rascunho
 * (inativo/não-visível) — a ativação pública do card é responsabilidade do
 * POST /roommates/perfil (T5.8.2).</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciasRoommateRequestDTO {

    @JsonProperty("horarioDorme")
    private LocalTime horarioDorme;

    @JsonProperty("horarioAcorda")
    private LocalTime horarioAcorda;

    @JsonProperty("nivelBarulho")
    private PerfilRoommate.NivelBarulho nivelBarulho;

    @JsonProperty("fumante")
    private Boolean fumante;

    @JsonProperty("aceitaPets")
    private Boolean aceitaPets;
}
