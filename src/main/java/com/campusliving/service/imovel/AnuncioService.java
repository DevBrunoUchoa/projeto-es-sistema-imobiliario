package com.campusliving.service.imovel;

import java.util.List;
import java.util.UUID;

import com.campusliving.dto.imovel.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.imovel.AnuncioRequestDTO;
import com.campusliving.dto.imovel.AnuncioResponseDTO;
import com.campusliving.dto.imovel.AnuncioStatusUpdateDTO;
import com.campusliving.dto.imovel.AnuncioUpdateRequestDTO;
import com.campusliving.dto.imovel.VisualizacaoPorDiaDTO;

public interface AnuncioService {

    /** T5.5.2 — RF-12: publica anúncio vinculado ao imóvel; bloqueia duplicidade de ativo por imóvel. */
    AnuncioResponseDTO publicar(AnuncioRequestDTO dto, UUID requesterId);

    /** T5.5.3 — RF-13: edita preço, descrição e regras de convivência. */
    AnuncioResponseDTO atualizar(UUID anuncioId, AnuncioUpdateRequestDTO dto, UUID requesterId);

    /** T5.5.4 — RF-14: inativação lógica (ATIVO/INATIVO/SUSPENSO). */
    AnuncioResponseDTO atualizarStatus(UUID anuncioId, AnuncioStatusUpdateDTO dto, UUID requesterId);

    /** T5.5.5 — RF-15: detalhes completos; incrementa visualizações quando o requester não é o dono. */
    AnuncioDetalhesResponseDTO getDetalhes(UUID anuncioId, UUID requesterId);

    /** T5.5.7 — RF-17: visualizações agrupadas por dia; exclusivo do dono (ou admin). */
    List<VisualizacaoPorDiaDTO> getEstatisticas(UUID anuncioId, UUID requesterId);
}
