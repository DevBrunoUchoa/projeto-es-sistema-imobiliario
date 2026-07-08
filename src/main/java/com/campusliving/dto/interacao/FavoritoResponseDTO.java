package com.campusliving.dto.interacao;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.campusliving.model.interacao.Favorito;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Deliberadamente enxuto: sem dados do anúncio em si (título, preço, foto...)
// porque não existe entidade de Ad ainda (T5.5). Quando o T5.5 existir, dá
// pra enriquecer este DTO com um resumo do anúncio sem mudar o contrato do
// endpoint (só adicionar campos).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritoResponseDTO {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("adId")
    private UUID adId;

    @JsonProperty("dataCriacao")
    private OffsetDateTime dataCriacao;

    public FavoritoResponseDTO(Favorito favorito) {
        this.id = favorito.getId();
        this.adId = favorito.getAdId();
        this.dataCriacao = favorito.getDataCriacao();
    }
}
