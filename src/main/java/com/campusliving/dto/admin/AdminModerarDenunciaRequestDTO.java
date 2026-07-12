package com.campusliving.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * RF-42 (escopo mínimo): ação de moderação sobre uma denúncia.
 * <ul>
 *   <li>{@code BANIR_ANUNCIO} — inativa o anúncio alvo e resolve a denúncia;</li>
 *   <li>{@code ARQUIVAR} — encerra a denúncia como improcedente, sem punição.</li>
 * </ul>
 */
@Data
public class AdminModerarDenunciaRequestDTO {

    @NotNull(message = "Ação é obrigatória")
    private Acao acao;

    public enum Acao {
        BANIR_ANUNCIO, ARQUIVAR
    }
}
