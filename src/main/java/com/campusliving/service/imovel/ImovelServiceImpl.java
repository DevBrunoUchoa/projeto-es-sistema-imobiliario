package com.campusliving.service.imovel;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.exception.imovel.TipoContaNaoPermiteImovelException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.integracao.CepValidationService;
import com.campusliving.service.integracao.GeocodingService;
import com.campusliving.service.integracao.GeocodingService.Coordenadas;

@Service
public class ImovelServiceImpl implements ImovelService {

    private final ImovelRepository imovelRepository;
    private final UserRepository userRepository;
    private final CepValidationService cepValidationService;
    private final GeocodingService geocodingService;

    public ImovelServiceImpl(
            ImovelRepository imovelRepository,
            UserRepository userRepository,
            CepValidationService cepValidationService,
            GeocodingService geocodingService
    ) {
        this.imovelRepository = imovelRepository;
        this.userRepository = userRepository;
        this.cepValidationService = cepValidationService;
        this.geocodingService = geocodingService;
    }

    @Override
    public ImovelResponseDTO criar(ImovelRequestDTO dto, UUID requesterId) {
        if (requesterId == null) {
            throw new AcessoNegadoException();
        }
        User proprietario = userRepository.findById(requesterId).orElseThrow(UserNotFoundException::new);
        // RF-11: só LOCADOR/MISTO cadastram imóveis — ESTUDANTE "puro" ainda
        // não fez a promoção de conta (T5.4.4) e não tem esse papel.
        if (proprietario.getTipoConta() != User.Tipo.LOCADOR && proprietario.getTipoConta() != User.Tipo.MISTO) {
            throw new TipoContaNaoPermiteImovelException();
        }

        cepValidationService.validarCampinaGrande(dto.getCep());

        String cidade = dto.getCidade() != null && !dto.getCidade().isBlank() ? dto.getCidade() : "Campina Grande";
        String estado = dto.getEstado() != null && !dto.getEstado().isBlank() ? dto.getEstado() : "PB";

        Coordenadas coordenadas = geocodingService.geocodificar(montarEnderecoCompleto(dto, cidade, estado));

        Imovel imovel = Imovel.builder()
                .proprietarioId(requesterId)
                .tipo(dto.getTipo())
                .cep(dto.getCep())
                .rua(dto.getRua())
                .numero(dto.getNumero())
                .complemento(dto.getComplemento())
                .bairro(dto.getBairro())
                .cidade(cidade)
                .estado(estado)
                .latitude(coordenadas.latitude())
                .longitude(coordenadas.longitude())
                .ativo(true)
                .build();

        imovelRepository.save(imovel);
        return new ImovelResponseDTO(imovel);
    }

    private String montarEnderecoCompleto(ImovelRequestDTO dto, String cidade, String estado) {
        StringBuilder endereco = new StringBuilder();
        endereco.append(dto.getRua()).append(", ").append(dto.getNumero());
        if (dto.getBairro() != null && !dto.getBairro().isBlank()) {
            endereco.append(", ").append(dto.getBairro());
        }
        endereco.append(", ").append(cidade).append(" - ").append(estado).append(", Brasil");
        return endereco.toString();
    }
}
