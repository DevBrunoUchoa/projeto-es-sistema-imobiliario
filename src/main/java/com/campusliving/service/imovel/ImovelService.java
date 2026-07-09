package com.campusliving.service.imovel;

import java.util.UUID;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;

public interface ImovelService {

    /** T5.5.1 — RF-11: valida CEP (Campina Grande-PB), geocodifica o endereço e cadastra o imóvel. */
    ImovelResponseDTO criar(ImovelRequestDTO dto, UUID requesterId);
}
