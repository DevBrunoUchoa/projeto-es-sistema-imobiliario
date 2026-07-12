package com.campusliving.service.admin;

import com.campusliving.dto.admin.AdminDenunciaResponseDTO;
import com.campusliving.dto.admin.AdminUsuarioResponseDTO;
import com.campusliving.dto.admin.AdminVerificarLocadorResponseDTO;
import com.campusliving.model.denuncia.Denuncia;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.dto.admin.AdminRelatorioResponseDTO;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.imovel.AnuncioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campusliving.repository.denuncia.DenunciaRepository;
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
    public AdminRelatorioResponseDTO gerarRelatorio() {
        long totalUsuarios = userRepository.count();
        long totalAnunciosAtivos = anuncioRepository.countByStatus(Anuncio.Status.ATIVO);
        long totalDenunciasPendentes = denunciaRepository.countByStatus(Denuncia.Status.PENDENTE);
        long totalDenunciasResolvidas = denunciaRepository.countByStatus(Denuncia.Status.RESOLVIDA);
        long totalLocadoresVerificados = userRepository.countByTipoContaAndVerificado(User.Tipo.LOCADOR, true);

        return AdminRelatorioResponseDTO.builder()
                .totalUsuarios(totalUsuarios)
                .totalAnunciosAtivos(totalAnunciosAtivos)
                .totalDenunciasPendentes(totalDenunciasPendentes)
                .totalDenunciasResolvidas(totalDenunciasResolvidas)
                .totalLocadoresVerificados(totalLocadoresVerificados)
                .mensagem("Relatório gerado com sucesso!")
                .build();
    }
}