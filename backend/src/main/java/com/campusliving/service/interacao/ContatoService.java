package com.campusliving.service.interacao;

import java.util.UUID;

import com.campusliving.dto.interacao.ContatoResponseDTO;
import com.campusliving.dto.interacao.InteresseRequestDTO;

public interface ContatoService {

    /** RF-28/RNF-LEG-03: registra interesse do estudante em um anúncio. */
    ContatoResponseDTO registrarInteresse(InteresseRequestDTO dto, UUID estudanteId);
}
