package com.campusliving.service.admin;

import com.campusliving.dto.admin.AdminDenunciaResponseDTO;
import com.campusliving.dto.admin.AdminModerarDenunciaRequestDTO;
import com.campusliving.dto.admin.AdminUsuarioResponseDTO;
import com.campusliving.dto.admin.AdminVerificarLocadorResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.denuncia.Denuncia;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.dto.admin.AdminRelatorioResponseDTO;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.imovel.AnuncioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusliving.repository.denuncia.DenunciaRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DenunciaRepository denunciaRepository;
    private final AnuncioRepository anuncioRepository;

    @Transactional(readOnly = true)
    public List<AdminUsuarioResponseDTO> listarUsuarios() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::mapToUsuarioResponse)
                .collect(Collectors.toList());
    }

    private AdminUsuarioResponseDTO mapToUsuarioResponse(User user) {
        return AdminUsuarioResponseDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .tipoConta(user.getTipoConta().name())
                .verificado(user.isVerificado())
                .ativo(user.isAtivo())
                .dataCriacao(user.getDataCriacao())
                .build();
    }

    public List<AdminDenunciaResponseDTO> listarDenuncias() {
    List<Denuncia> denuncias = denunciaRepository.findAll();

    return denuncias.stream()
            .map(this::mapToDenunciaResponse)
            .collect(Collectors.toList());
    }

    private AdminDenunciaResponseDTO mapToDenunciaResponse(Denuncia denuncia) {
        String denuncianteNome = userRepository.findById(denuncia.getDenuncianteId())
                .map(User::getNome)
                .orElse("Usuário não encontrado");

        return AdminDenunciaResponseDTO.builder()
                .id(denuncia.getId())
                .denuncianteId(denuncia.getDenuncianteId())
                .denuncianteNome(denuncianteNome)
                .tipoAlvo(denuncia.getTipoAlvo().name())
                .alvoId(denuncia.getAlvoId())
                .motivo(denuncia.getMotivo().name())
                .descricao(denuncia.getDescricao())
                .status(denuncia.getStatus().name())
                .contadorDenuncias(denuncia.getContadorDenuncias())
                .dataCriacao(denuncia.getDataCriacao())
                .build();
    }

    @Transactional
    public AdminVerificarLocadorResponseDTO verificarLocador(UUID userId, Boolean verificado) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Verificar se o usuário é LOCADOR
        if (user.getTipoConta() != User.Tipo.LOCADOR) {
            throw new RuntimeException("Apenas usuários do tipo LOCADOR podem ser verificados");
        }

        user.setVerificado(verificado);
        userRepository.save(user);

        return AdminVerificarLocadorResponseDTO.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .tipoConta(user.getTipoConta().name())
                .verificado(user.isVerificado())
                .mensagem("Status de verificação atualizado com sucesso!")
                .build();
    }

    @Transactional(readOnly = true)
    public AdminRelatorioResponseDTO gerarRelatorio(int periodoDias) {
        int dias = periodoDias <= 0 ? 30 : periodoDias;
        OffsetDateTime inicioPeriodo = OffsetDateTime.now().minusDays(dias);

        return AdminRelatorioResponseDTO.builder()
                .totalUsuarios(userRepository.count())
                .totalAnunciosAtivos(anuncioRepository.countByStatus(Anuncio.Status.ATIVO))
                .totalDenunciasPendentes(denunciaRepository.countByStatus(Denuncia.Status.PENDENTE))
                .totalDenunciasResolvidas(denunciaRepository.countByStatus(Denuncia.Status.RESOLVIDA))
                .totalLocadoresVerificados(userRepository.countByTipoContaAndVerificado(User.Tipo.LOCADOR, true))
                .periodoDias(dias)
                .novosCadastrosPeriodo(userRepository.countByDataCriacaoAfter(inicioPeriodo))
                .anunciosPublicadosPeriodo(anuncioRepository.countByDataPublicacaoAfter(inicioPeriodo))
                .denunciasPeriodo(denunciaRepository.countByDataCriacaoAfter(inicioPeriodo))
                .mensagem("Relatório gerado com sucesso!")
                .build();
    }

    /** RF-43: exporta o relatório do período em CSV. */
    @Transactional(readOnly = true)
    public String gerarRelatorioCsv(int periodoDias) {
        AdminRelatorioResponseDTO r = gerarRelatorio(periodoDias);
        StringBuilder csv = new StringBuilder("metrica,valor\n");
        csv.append("periodo_dias,").append(r.getPeriodoDias()).append('\n');
        csv.append("total_usuarios,").append(r.getTotalUsuarios()).append('\n');
        csv.append("total_anuncios_ativos,").append(r.getTotalAnunciosAtivos()).append('\n');
        csv.append("total_denuncias_pendentes,").append(r.getTotalDenunciasPendentes()).append('\n');
        csv.append("total_denuncias_resolvidas,").append(r.getTotalDenunciasResolvidas()).append('\n');
        csv.append("total_locadores_verificados,").append(r.getTotalLocadoresVerificados()).append('\n');
        csv.append("novos_cadastros_periodo,").append(r.getNovosCadastrosPeriodo()).append('\n');
        csv.append("anuncios_publicados_periodo,").append(r.getAnunciosPublicadosPeriodo()).append('\n');
        csv.append("denuncias_periodo,").append(r.getDenunciasPeriodo()).append('\n');
        return csv.toString();
    }

    /**
     * RF-42 (escopo mínimo): aplica a ação de moderação sobre a denúncia.
     * BANIR_ANUNCIO inativa o anúncio alvo; ARQUIVAR encerra sem punição.
     */
    @Transactional
    public AdminDenunciaResponseDTO moderarDenuncia(UUID denunciaId,
            AdminModerarDenunciaRequestDTO.Acao acao, UUID adminId) {
        Denuncia denuncia = denunciaRepository.findById(denunciaId)
                .orElseThrow(() -> new ProjectException("Denúncia não encontrada", HttpStatus.NOT_FOUND));

        if (acao == AdminModerarDenunciaRequestDTO.Acao.BANIR_ANUNCIO) {
            if (denuncia.getTipoAlvo() != Denuncia.TipoAlvo.ANUNCIO) {
                throw new ProjectException("Só é possível banir denúncias de anúncio", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            Anuncio anuncio = anuncioRepository.findById(denuncia.getAlvoId())
                    .orElseThrow(() -> new ProjectException("Anúncio alvo não encontrado", HttpStatus.NOT_FOUND));
            anuncio.setStatus(Anuncio.Status.INATIVO);
            anuncioRepository.save(anuncio);
            denuncia.setStatus(Denuncia.Status.RESOLVIDA);
        } else { // ARQUIVAR
            denuncia.setStatus(Denuncia.Status.REJEITADA);
        }

        denuncia.setResolvidoPor(adminId);
        denuncia.setDataResolucao(OffsetDateTime.now());
        denunciaRepository.save(denuncia);

        return mapToDenunciaResponse(denuncia);
    }
}