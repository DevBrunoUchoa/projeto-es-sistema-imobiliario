package com.campusliving.service.denuncia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.dto.denuncia.DenunciaRequestDTO;
import com.campusliving.dto.denuncia.DenunciaResponseDTO;
import com.campusliving.model.denuncia.Denuncia;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.denuncia.DenunciaRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;

/** Testes unitários das denúncias e da ocultação automática (RF-36/RF-37). */
@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {

    @Mock
    private DenunciaRepository denunciaRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private AuditLogService auditLogService;

    private DenunciaService service;

    @BeforeEach
    void setUp() {
        service = new DenunciaService(denunciaRepository, userRepository, anuncioRepository, auditLogService);
    }

    @Test
    void criarDenuncia_quandoValida_devePersistirMapearRespostaEAuditar() {
        UUID denuncianteId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();
        UUID denunciaId = UUID.randomUUID();
        OffsetDateTime criadaEm = OffsetDateTime.parse("2026-07-15T12:00:00-03:00");
        String email = "estudante@ufcg.edu.br";
        DenunciaRequestDTO request = denunciaRequest("anuncio", alvoId, "fraude");
        User denunciante = User.builder().id(denuncianteId).email(email).build();

        when(userRepository.findByEmail(email)).thenReturn(List.of(denunciante));
        when(denunciaRepository.findByDenuncianteIdAndAlvoIdAndStatus(
                denuncianteId, alvoId, Denuncia.Status.PENDENTE)).thenReturn(List.of());
        when(denunciaRepository.save(any(Denuncia.class))).thenAnswer(invocation -> {
            Denuncia denuncia = invocation.getArgument(0);
            denuncia.setId(denunciaId);
            denuncia.setDataCriacao(criadaEm);
            return denuncia;
        });
        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(1L);

        DenunciaResponseDTO resposta = service.criarDenuncia(request, email);

        assertThat(resposta.getId()).isEqualTo(denunciaId);
        assertThat(resposta.getDenuncianteId()).isEqualTo(denuncianteId);
        assertThat(resposta.getTipoAlvo()).isEqualTo("ANUNCIO");
        assertThat(resposta.getAlvoId()).isEqualTo(alvoId);
        assertThat(resposta.getMotivo()).isEqualTo("FRAUDE");
        assertThat(resposta.getDescricao()).isEqualTo("Possível anúncio falso");
        assertThat(resposta.getStatus()).isEqualTo("PENDENTE");
        assertThat(resposta.getContadorDenuncias()).isEqualTo(1);
        assertThat(resposta.getDataCriacao()).isEqualTo(criadaEm);
        assertThat(resposta.getMensagem()).isEqualTo("Denúncia criada com sucesso!");

        ArgumentCaptor<Denuncia> denunciaCaptor = ArgumentCaptor.forClass(Denuncia.class);
        verify(denunciaRepository).save(denunciaCaptor.capture());
        Denuncia denunciaPersistida = denunciaCaptor.getValue();
        assertThat(denunciaPersistida.getDenuncianteId()).isEqualTo(denuncianteId);
        assertThat(denunciaPersistida.getTipoAlvo()).isEqualTo(Denuncia.TipoAlvo.ANUNCIO);
        assertThat(denunciaPersistida.getAlvoId()).isEqualTo(alvoId);
        assertThat(denunciaPersistida.getMotivo()).isEqualTo(Denuncia.Motivo.FRAUDE);
        assertThat(denunciaPersistida.getStatus()).isEqualTo(Denuncia.Status.PENDENTE);
        assertThat(denunciaPersistida.getContadorDenuncias()).isEqualTo(1);
        verify(auditLogService).registrarAcao(denuncianteId, "CRIAR_DENUNCIA", "Denuncia", denunciaId);
        verify(anuncioRepository, never()).findById(any(UUID.class));
    }

    @Test
    void criarDenuncia_quandoUsuarioNaoEncontrado_deveInterromperSemPersistir() {
        DenunciaRequestDTO request = denunciaRequest("ANUNCIO", UUID.randomUUID(), "SPAM");

        when(userRepository.findByEmail("inexistente@ufcg.edu.br")).thenReturn(List.of());

        assertThatThrownBy(() -> service.criarDenuncia(request, "inexistente@ufcg.edu.br"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Usuário não encontrado");

        verifyNoInteractions(denunciaRepository, anuncioRepository, auditLogService);
    }

    @Test
    void criarDenuncia_quandoTipoAlvoInvalido_deveInterromperSemConsultarDenuncias() {
        String email = "estudante@ufcg.edu.br";
        DenunciaRequestDTO request = denunciaRequest("IMOVEL", UUID.randomUUID(), "SPAM");

        when(userRepository.findByEmail(email)).thenReturn(List.of(User.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.criarDenuncia(request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Tipo de alvo inválido. Valores: ANUNCIO, USUARIO");

        verifyNoInteractions(denunciaRepository, anuncioRepository, auditLogService);
    }

    @Test
    void criarDenuncia_quandoMotivoInvalido_deveInterromperSemConsultarDenuncias() {
        String email = "estudante@ufcg.edu.br";
        DenunciaRequestDTO request = denunciaRequest("USUARIO", UUID.randomUUID(), "PROPAGANDA");

        when(userRepository.findByEmail(email)).thenReturn(List.of(User.builder().id(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> service.criarDenuncia(request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Motivo inválido. Valores: CONTEUDO_INADEQUADO, SPAM, FRAUDE, ASSEDIO, OUTROS");

        verifyNoInteractions(denunciaRepository, anuncioRepository, auditLogService);
    }

    @Test
    void criarDenuncia_quandoJaHaDenunciaPendenteDoMesmoUsuario_deveRejeitarDuplicata() {
        UUID denuncianteId = UUID.randomUUID();
        UUID alvoId = UUID.randomUUID();
        String email = "estudante@ufcg.edu.br";
        DenunciaRequestDTO request = denunciaRequest("ANUNCIO", alvoId, "SPAM");
        Denuncia existente = Denuncia.builder().id(UUID.randomUUID()).status(Denuncia.Status.PENDENTE).build();

        when(userRepository.findByEmail(email)).thenReturn(List.of(User.builder().id(denuncianteId).build()));
        when(denunciaRepository.findByDenuncianteIdAndAlvoIdAndStatus(
                denuncianteId, alvoId, Denuncia.Status.PENDENTE)).thenReturn(List.of(existente));

        assertThatThrownBy(() -> service.criarDenuncia(request, email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Você já denunciou este alvo. Aguarde a análise.");

        verify(denunciaRepository, never()).save(any(Denuncia.class));
        verifyNoInteractions(anuncioRepository, auditLogService);
    }

    @Test
    void verificarEOcultar_quandoAtingeCincoDenunciasDeAnuncioAtivo_deveInativarEAuditar() {
        UUID alvoId = UUID.randomUUID();
        Anuncio anuncio = Anuncio.builder().id(alvoId).status(Anuncio.Status.ATIVO).build();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(5L);
        when(anuncioRepository.findById(alvoId)).thenReturn(Optional.of(anuncio));

        service.verificarEOcultar(alvoId, Denuncia.TipoAlvo.ANUNCIO);

        assertThat(anuncio.getStatus()).isEqualTo(Anuncio.Status.INATIVO);
        verify(anuncioRepository).save(anuncio);
        verify(auditLogService).registrarAcao(
                isNull(), eq("OCULTAR_ANUNCIO_AUTOMATICO"), eq("Anuncio"), eq(alvoId));
    }

    @Test
    void verificarEOcultar_quandoAindaHaMenosDeCincoDenuncias_naoConsultaAnuncio() {
        UUID alvoId = UUID.randomUUID();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(4L);

        service.verificarEOcultar(alvoId, Denuncia.TipoAlvo.ANUNCIO);

        verifyNoInteractions(anuncioRepository, auditLogService);
    }

    @Test
    void verificarEOcultar_quandoAnuncioJaEstaInativo_naoPersisteNemAuditaNovamente() {
        UUID alvoId = UUID.randomUUID();
        Anuncio anuncio = Anuncio.builder().id(alvoId).status(Anuncio.Status.INATIVO).build();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(6L);
        when(anuncioRepository.findById(alvoId)).thenReturn(Optional.of(anuncio));

        service.verificarEOcultar(alvoId, Denuncia.TipoAlvo.ANUNCIO);

        assertThat(anuncio.getStatus()).isEqualTo(Anuncio.Status.INATIVO);
        verify(anuncioRepository, never()).save(any(Anuncio.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void verificarEOcultar_quandoAnuncioNaoExisteAposLimite_deveInformarErro() {
        UUID alvoId = UUID.randomUUID();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(5L);
        when(anuncioRepository.findById(alvoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarEOcultar(alvoId, Denuncia.TipoAlvo.ANUNCIO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Anúncio não encontrado");

        verify(anuncioRepository, never()).save(any(Anuncio.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void verificarEOcultar_quandoAlvoEhUsuario_naoTocaEmAnuncios() {
        UUID alvoId = UUID.randomUUID();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(5L);

        service.verificarEOcultar(alvoId, Denuncia.TipoAlvo.USUARIO);

        verifyNoInteractions(anuncioRepository, auditLogService);
    }

    @Test
    void contarDenuncias_deveContarSomentePendentesDoAlvo() {
        UUID alvoId = UUID.randomUUID();

        when(denunciaRepository.countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE)).thenReturn(3L);

        assertThat(service.contarDenuncias(alvoId)).isEqualTo(3L);
        verify(denunciaRepository).countByAlvoIdAndStatus(alvoId, Denuncia.Status.PENDENTE);
    }

    private DenunciaRequestDTO denunciaRequest(String tipoAlvo, UUID alvoId, String motivo) {
        DenunciaRequestDTO request = new DenunciaRequestDTO();
        request.setTipoAlvo(tipoAlvo);
        request.setAlvoId(alvoId.toString());
        request.setMotivo(motivo);
        request.setDescricao("Possível anúncio falso");
        return request;
    }
}
