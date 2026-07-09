package com.campusliving.dto.imovel;

import java.util.List;

import com.campusliving.model.imovel.Anuncio;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corpo de GET /anuncios/:id (RF-15) — os campos do anúncio (composição, não
 * herança, pra não duplicar os ~18 campos de {@link AnuncioResponseDTO})
 * mais imagens e nota média, que não são colunas do próprio anúncio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnuncioDetalhesResponseDTO {

    @JsonProperty("anuncio")
    private AnuncioResponseDTO anuncio;

    @JsonProperty("imagensUrls")
    private List<String> imagensUrls;

    // null quando o anúncio ainda não tem nenhuma avaliação.
    @JsonProperty("notaMedia")
    private Double notaMedia;

    public AnuncioDetalhesResponseDTO(Anuncio anuncio, List<String> imagensUrls, Double notaMedia) {
        this.anuncio = new AnuncioResponseDTO(anuncio);
        this.imagensUrls = imagensUrls;
        this.notaMedia = notaMedia;
    }
}
