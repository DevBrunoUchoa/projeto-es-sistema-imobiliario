package com.campusliving.service.imovel;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import com.campusliving.service.geocoding.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImovelService {

    private final ImovelRepository imovelRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final GeocodingService geocodingService;

    @Transactional
    public ImovelResponseDTO criarImovel(ImovelRequestDTO request, String email) {
        // Buscar o proprietário pelo email
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new RuntimeException("Proprietário não encontrado");
        }
        User proprietario = users.get(0);
        UUID proprietarioId = proprietario.getId();

        // Verificar se o proprietário é LOCADOR ou ADMIN
        if (!"LOCADOR".equals(proprietario.getTipoConta().name()) &&
            !"ADMIN".equals(proprietario.getTipoConta().name())) {
            throw new RuntimeException("Apenas LOCADOR ou ADMIN podem criar imóveis");
        }

        // RF-16: se o cliente não informou coordenadas, geocodifica o endereço
        // automaticamente (Nominatim). Coordenadas explícitas têm prioridade.
        Double latitude = request.getLatitude();
        Double longitude = request.getLongitude();
        if (latitude == null || longitude == null) {
            GeocodingService.Coordenadas coord = geocodingService
                    .geocodificar(montarEnderecoCompleto(request))
                    .orElseThrow(() -> new ProjectException(
                            "Não foi possível localizar o endereço. Informe latitude e longitude.",
                            HttpStatus.UNPROCESSABLE_ENTITY));
            latitude = coord.latitude();
            longitude = coord.longitude();
        }

        // Criar o imóvel
        Imovel imovel = Imovel.builder()
                .proprietarioId(proprietarioId)
                .tipo(Imovel.Tipo.valueOf(request.getTipo()))
                .cep(request.getCep().replace("-", ""))
                .rua(request.getRua())
                .numero(request.getNumero())
                .complemento(request.getComplemento())
                .bairro(request.getBairro())
                .cidade(request.getCidade())
                .estado(request.getEstado())
                .latitude(latitude)
                .longitude(longitude)
                .ativo(true)
                .build();

        Imovel savedImovel = imovelRepository.save(imovel);

        // Registrar log
        auditLogService.registrarAcao(
                proprietarioId,
                "CRIAR_IMOVEL",
                "Imovel",
                savedImovel.getId()
        );

        return ImovelResponseDTO.builder()
                .id(savedImovel.getId())
                .proprietarioId(savedImovel.getProprietarioId())
                .tipo(savedImovel.getTipo().name())
                .cep(savedImovel.getCep())
                .rua(savedImovel.getRua())
                .numero(savedImovel.getNumero())
                .complemento(savedImovel.getComplemento())
                .bairro(savedImovel.getBairro())
                .cidade(savedImovel.getCidade())
                .estado(savedImovel.getEstado())
                .latitude(savedImovel.getLatitude())
                .longitude(savedImovel.getLongitude())
                .ativo(savedImovel.isAtivo())
                .dataCriacao(savedImovel.getDataCriacao())
                .mensagem("Imóvel criado com sucesso!")
                .build();
    }

    private String montarEnderecoCompleto(ImovelRequestDTO request) {
        return String.join(", ",
                request.getRua() + ", " + request.getNumero(),
                request.getBairro(),
                request.getCidade(),
                request.getEstado(),
                request.getCep());
    }
}