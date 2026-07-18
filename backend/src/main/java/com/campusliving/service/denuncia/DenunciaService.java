package com.campusliving.service.denuncia;

import com.campusliving.dto.denuncia.DenunciaRequestDTO;
import com.campusliving.dto.denuncia.DenunciaResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.denuncia.Denuncia;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.denuncia.DenunciaRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final UserRepository userRepository;
    private final AnuncioRepository anuncioRepository;
    private final AuditLogService auditLogService;

    private static final int LIMITE_DENUNCIAS_OCULTAR = 5;

    @Transactional
    public DenunciaResponseDTO criarDenuncia(DenunciaRequestDTO request, String email) {
        System.out.println(">>> CRIANDO DENÚNCIA para: " + request.getAlvoId());
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new ProjectException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        User denunciante = users.get(0);

        Denuncia.TipoAlvo tipoAlvo;
        try {
            tipoAlvo = Denuncia.TipoAlvo.valueOf(request.getTipoAlvo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProjectException("Tipo de alvo inválido. Valores: ANUNCIO, USUARIO", HttpStatus.BAD_REQUEST);
        }

        Denuncia.Motivo motivo;
        try {
            motivo = Denuncia.Motivo.valueOf(request.getMotivo().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ProjectException("Motivo inválido. Valores: CONTEUDO_INADEQUADO, SPAM, FRAUDE, ASSEDIO, OUTROS", HttpStatus.BAD_REQUEST);
        }

        UUID alvoId = UUID.fromString(request.getAlvoId());

        List<Denuncia> denunciasExistentes = denunciaRepository.findByDenuncianteIdAndAlvoIdAndStatus(
                denunciante.getId(), alvoId, Denuncia.Status.PENDENTE);
        if (!denunciasExistentes.isEmpty()) {
            throw new ProjectException("Você já denunciou este alvo. Aguarde a análise.", HttpStatus.CONFLICT);
        }

        Denuncia denuncia = Denuncia.builder()
                .denuncianteId(denunciante.getId())
                .tipoAlvo(tipoAlvo)
                .alvoId(alvoId)
                .motivo(motivo)
                .descricao(request.getDescricao())
                .status(Denuncia.Status.PENDENTE)
                .contadorDenuncias(1)
                .build();

        Denuncia savedDenuncia = denunciaRepository.save(denuncia);

        verificarEOcultar(alvoId, tipoAlvo);

        auditLogService.registrarAcao(
                denunciante.getId(),
                "CRIAR_DENUNCIA",
                "Denuncia",
                savedDenuncia.getId()
        );

        return DenunciaResponseDTO.builder()
                .id(savedDenuncia.getId())
                .denuncianteId(savedDenuncia.getDenuncianteId())
                .tipoAlvo(savedDenuncia.getTipoAlvo().name())
                .alvoId(savedDenuncia.getAlvoId())
                .motivo(savedDenuncia.getMotivo().name())
                .descricao(savedDenuncia.getDescricao())
                .status(savedDenuncia.getStatus().name())
                .contadorDenuncias(savedDenuncia.getContadorDenuncias())
                .dataCriacao(savedDenuncia.getDataCriacao())
                .mensagem("Denúncia criada com sucesso!")
                .build();
    }

    @Transactional
    public void verificarEOcultar(UUID alvoId, Denuncia.TipoAlvo tipoAlvo) {
        long totalDenuncias = denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE);

        if (totalDenuncias >= LIMITE_DENUNCIAS_OCULTAR) {
            if (tipoAlvo == Denuncia.TipoAlvo.ANUNCIO) {

                Anuncio anuncio = anuncioRepository.findById(alvoId)
                        .orElseThrow(() -> new ProjectException("Anúncio não encontrado", HttpStatus.NOT_FOUND));

                if (anuncio.getStatus() == Anuncio.Status.ATIVO) {
                    anuncio.setStatus(Anuncio.Status.INATIVO);
                    anuncioRepository.save(anuncio);

                    auditLogService.registrarAcao(
                            null,
                            "OCULTAR_ANUNCIO_AUTOMATICO",
                            "Anuncio",
                            alvoId
                    );
                }
            } else if (tipoAlvo == Denuncia.TipoAlvo.USUARIO) {

            }
        }
    }

    @Transactional(readOnly = true)
public long contarDenuncias(UUID alvoId) {
    return denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE);
}
}