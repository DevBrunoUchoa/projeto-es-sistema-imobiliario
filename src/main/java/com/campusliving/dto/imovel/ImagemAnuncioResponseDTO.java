package com.campusliving.dto.imovel;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.campusliving.model.imovel.ImagemAnuncio;
import lombok.Getter;

@Getter
public class ImagemAnuncioResponseDTO {
    private final UUID id;
    private final UUID adId;
    private final String url;
    private final Integer ordem;
    private final boolean principal;
    private final OffsetDateTime dataCriacao;

    public ImagemAnuncioResponseDTO(ImagemAnuncio imagem) {
        id = imagem.getId(); adId = imagem.getAdId(); url = imagem.getUrl();
        ordem = imagem.getOrdem(); principal = imagem.isPrincipal();
        dataCriacao = imagem.getDataCriacao();
    }
}
