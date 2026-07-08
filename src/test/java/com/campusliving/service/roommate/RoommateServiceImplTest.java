package com.campusliving.service.roommate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

import com.campusliving.dto.roommate.PerfilRoommateRequestDTO;
import com.campusliving.dto.roommate.PerfilRoommateResponseDTO;
import com.campusliving.dto.roommate.PreferenciasRoommateRequestDTO;
import com.campusliving.dto.roommate.RoommateCompativelDTO;
import com.campusliving.dto.roommate.RoommateMatchRequestDTO;
import com.campusliving.dto.roommate.RoommateMatchResponseDTO;
import com.campusliving.dto.roommate.RoommateMatchStatusUpdateDTO;
import com.campusliving.exception.roommate.AutoMatchException;
import com.campusliving.exception.roommate.MatchDuplicadoException;
import com.campusliving.exception.roommate.MatchNaoEncontradoException;
import com.campusliving.exception.roommate.PerfilRoommateNaoEncontradoException;
import com.campusliving.exception.roommate.StatusMatchInvalidoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.notificacao.Notificacao;
import com.campusliving.model.roommate.PerfilRoommate;
import com.campusliving.model.roommate.RoommateMatch;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.notificacao.NotificacaoRepository;
import com.campusliving.repository.roommate.PerfilRoommateRepository;
import com.campusliving.repository.roommate.RoommateMatchRepository;
import com.campusliving.repository.usuario.UserRepository;

@ExtendWith(MockitoExtension.class)
class RoommateServiceImplTest {

    @Mock
    private PerfilRoommateRepository perfilRoommateRepository;
    @Mock
    private RoommateMatchRepository roommateMatchRepository;
    @Mock
    private NotificacaoRepository notificacaoRepository;
    @Mock
    private UserRepository userRepository;

    private RoommateServiceImpl service;

    private UUID meuId;
    private UUID adminId;
    private UUID estranhoId;
    private User admin;
    private User estranho;

    @BeforeEach
    void setUp() {
        service = new RoommateServiceImpl(perfilRoommateRepository, roommateMatchRepository, notificacaoRepository, userRepository);

        meuId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        estranhoId = UUID.randomUUID();

        admin = User.builder().id(adminId).nome("Admin").tipoConta(User.Tipo.ADMIN).build();
        estranho = User.builder().id(estranhoId).nome("Estranho").tipoConta(User.Tipo.ESTUDANTE).build();
    }

    // --- salvarPreferencias (T5.8.1 / RF-10/RF-32) ------------------------

    @Test
    void salvarPreferencias_quandoNaoExistePerfil_deveCriarRascunho() {
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.empty());

        PreferenciasRoommateRequestDTO dto = PreferenciasRoommateRequestDTO.builder()
                .fumante(false)
                .aceitaPets(true)
                .nivelBarulho(PerfilRoommate.NivelBarulho.SILENCIOSO)
                .build();

        PerfilRoommateResponseDTO resultado = service.salvarPreferencias(meuId, dto, meuId);

        assertThat(resultado.getUserId()).isEqualTo(meuId);
        assertThat(resultado.isAceitaPets()).isTrue();
        assertThat(resultado.getNivelBarulhoPreferido()).isEqualTo(PerfilRoommate.NivelBarulho.SILENCIOSO);
        assertThat(resultado.isAtivo()).isFalse();
        verify(perfilRoommateRepository).save(any(PerfilRoommate.class));
    }

    @Test
    void salvarPreferencias_quandoJaExistePerfil_deveAtualizarApenasCamposEnviados() {
        PerfilRoommate existente = PerfilRoommate.builder()
                .id(UUID.randomUUID())
                .userId(meuId)
                .fumante(true)
                .aceitaPets(false)
                .build();

        when(userRepository.existsById(meuId)).thenReturn(true);
        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.of(existente));

        PreferenciasRoommateRequestDTO dto = PreferenciasRoommateRequestDTO.builder()
                .aceitaPets(true)
                .build();

        PerfilRoommateResponseDTO resultado = service.salvarPreferencias(meuId, dto, meuId);

        assertThat(resultado.isAceitaPets()).isTrue();
        // não enviado no DTO -> preserva o valor anterior
        assertThat(resultado.isFumante()).isTrue();
    }

    @Test
    void salvarPreferencias_quandoNaoEhDonoNemAdmin_deveLancarAcessoNegado() {
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));

        assertThatThrownBy(() -> service.salvarPreferencias(meuId, PreferenciasRoommateRequestDTO.builder().build(), estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(perfilRoommateRepository, never()).save(any());
    }

    @Test
    void salvarPreferencias_quandoUsuarioNaoExiste_deveLancarUserNotFound() {
        when(userRepository.existsById(meuId)).thenReturn(false);

        assertThatThrownBy(() -> service.salvarPreferencias(meuId, PreferenciasRoommateRequestDTO.builder().build(), meuId))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- ativarPerfil (T5.8.2 / RF-32) ------------------------------------

    @Test
    void ativarPerfil_quandoNovoPerfil_deveCriarEAtivar() {
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.empty());

        PerfilRoommateRequestDTO dto = PerfilRoommateRequestDTO.builder()
                .descricao("Procuro colega tranquilo")
                .orcamentoMax(new BigDecimal("800.00"))
                .jaPossuiCasa(true)
                .perfilVisivel(true)
                .build();

        PerfilRoommateResponseDTO resultado = service.ativarPerfil(dto, meuId);

        assertThat(resultado.isAtivo()).isTrue();
        assertThat(resultado.isJaPossuiCasa()).isTrue();
        assertThat(resultado.isPerfilVisivel()).isTrue();
        assertThat(resultado.getDescricao()).isEqualTo("Procuro colega tranquilo");
        verify(perfilRoommateRepository).save(any(PerfilRoommate.class));
    }

    @Test
    void ativarPerfil_quandoPerfilExistenteInativo_deveReativar() {
        PerfilRoommate existente = PerfilRoommate.builder()
                .id(UUID.randomUUID())
                .userId(meuId)
                .ativo(false)
                .perfilVisivel(false)
                .build();

        when(userRepository.existsById(meuId)).thenReturn(true);
        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.of(existente));

        PerfilRoommateRequestDTO dto = PerfilRoommateRequestDTO.builder()
                .perfilVisivel(true)
                .build();

        PerfilRoommateResponseDTO resultado = service.ativarPerfil(dto, meuId);

        assertThat(resultado.isAtivo()).isTrue();
        assertThat(resultado.isPerfilVisivel()).isTrue();
    }

    @Test
    void ativarPerfil_semRequester_deveLancarAcessoNegado() {
        assertThatThrownBy(() -> service.ativarPerfil(PerfilRoommateRequestDTO.builder().build(), null))
                .isInstanceOf(AcessoNegadoException.class);
        verify(perfilRoommateRepository, never()).save(any());
    }

    // --- listarCompativeis (T5.8.3 / RF-33) -------------------------------

    @Test
    void listarCompativeis_deveCalcularScoreEOrdenarDecrescente() {
        PerfilRoommate meuPerfil = PerfilRoommate.builder()
                .userId(meuId)
                .orcamentoMax(new BigDecimal("1000.00"))
                .nivelBarulhoPreferido(PerfilRoommate.NivelBarulho.MODERADO)
                .fumante(false)
                .aceitaPets(true)
                .dataEntradaDesejada(LocalDate.of(2026, 8, 1))
                .build();

        UUID cand1Id = UUID.randomUUID();
        UUID cand2Id = UUID.randomUUID();

        // match "perfeito": todos os 5 critérios batem -> score 100
        PerfilRoommate candidato1 = PerfilRoommate.builder()
                .userId(cand1Id)
                .orcamentoMax(new BigDecimal("1000.00"))
                .nivelBarulhoPreferido(PerfilRoommate.NivelBarulho.MODERADO)
                .fumante(false)
                .aceitaPets(true)
                .dataEntradaDesejada(LocalDate.of(2026, 8, 1))
                .build();

        // match parcial: só pets e data batem (2 de 5) -> score 40
        PerfilRoommate candidato2 = PerfilRoommate.builder()
                .userId(cand2Id)
                .orcamentoMax(new BigDecimal("2000.00"))
                .nivelBarulhoPreferido(PerfilRoommate.NivelBarulho.AGITADO)
                .fumante(true)
                .aceitaPets(true)
                .dataEntradaDesejada(LocalDate.of(2026, 8, 1))
                .build();

        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.of(meuPerfil));
        when(perfilRoommateRepository.findByAtivoTrueAndPerfilVisivelTrueAndUserIdNot(meuId))
                .thenReturn(List.of(candidato2, candidato1)); // fora de ordem de propósito
        when(userRepository.findById(cand1Id)).thenReturn(Optional.of(
                User.builder().id(cand1Id).nome("Candidato Perfeito").tipoConta(User.Tipo.ESTUDANTE).build()));
        when(userRepository.findById(cand2Id)).thenReturn(Optional.of(
                User.builder().id(cand2Id).nome("Candidato Parcial").tipoConta(User.Tipo.ESTUDANTE).build()));

        List<RoommateCompativelDTO> resultado = service.listarCompativeis(meuId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getUserId()).isEqualTo(cand1Id);
        assertThat(resultado.get(0).getScoreCompatibilidade()).isEqualTo(100);
        assertThat(resultado.get(1).getUserId()).isEqualTo(cand2Id);
        assertThat(resultado.get(1).getScoreCompatibilidade()).isEqualTo(40);
    }

    @Test
    void listarCompativeis_quandoSemPerfilProprio_deveLancarPerfilNaoEncontrado() {
        when(perfilRoommateRepository.findByUserId(meuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listarCompativeis(meuId))
                .isInstanceOf(PerfilRoommateNaoEncontradoException.class);
    }

    @Test
    void listarCompativeis_semRequester_deveLancarAcessoNegado() {
        assertThatThrownBy(() -> service.listarCompativeis(null))
                .isInstanceOf(AcessoNegadoException.class);
    }

    // --- solicitarMatch (T5.8.4 / RF-34) ----------------------------------

    @Test
    void solicitarMatch_quandoValido_deveCriarPendente() {
        UUID destId = UUID.randomUUID();
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(userRepository.existsById(destId)).thenReturn(true);
        when(roommateMatchRepository.findByParEmQualquerSentido(meuId, destId)).thenReturn(List.of());

        RoommateMatchRequestDTO dto = RoommateMatchRequestDTO.builder()
                .destinatarioId(destId)
                .mensagemInicial("Bora dividir apê?")
                .build();

        RoommateMatchResponseDTO resultado = service.solicitarMatch(dto, meuId);

        assertThat(resultado.getSolicitanteId()).isEqualTo(meuId);
        assertThat(resultado.getDestinatarioId()).isEqualTo(destId);
        assertThat(resultado.getStatus()).isEqualTo(RoommateMatch.Status.PENDENTE.name());
        verify(roommateMatchRepository).save(any(RoommateMatch.class));
    }

    @Test
    void solicitarMatch_quandoAutoMatch_deveLancarException() {
        RoommateMatchRequestDTO dto = RoommateMatchRequestDTO.builder().destinatarioId(meuId).build();

        assertThatThrownBy(() -> service.solicitarMatch(dto, meuId))
                .isInstanceOf(AutoMatchException.class);
        verify(roommateMatchRepository, never()).save(any());
    }

    @Test
    void solicitarMatch_quandoJaExisteMatchPendente_deveLancarDuplicado() {
        UUID destId = UUID.randomUUID();
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(userRepository.existsById(destId)).thenReturn(true);

        RoommateMatch existente = RoommateMatch.builder()
                .solicitanteId(destId)
                .destinatarioId(meuId)
                .status(RoommateMatch.Status.PENDENTE)
                .build();
        when(roommateMatchRepository.findByParEmQualquerSentido(meuId, destId)).thenReturn(List.of(existente));

        RoommateMatchRequestDTO dto = RoommateMatchRequestDTO.builder().destinatarioId(destId).build();

        assertThatThrownBy(() -> service.solicitarMatch(dto, meuId))
                .isInstanceOf(MatchDuplicadoException.class);
        verify(roommateMatchRepository, never()).save(any());
    }

    @Test
    void solicitarMatch_quandoDestinatarioNaoExiste_deveLancarUserNotFound() {
        UUID destId = UUID.randomUUID();
        when(userRepository.existsById(meuId)).thenReturn(true);
        when(userRepository.existsById(destId)).thenReturn(false);

        RoommateMatchRequestDTO dto = RoommateMatchRequestDTO.builder().destinatarioId(destId).build();

        assertThatThrownBy(() -> service.solicitarMatch(dto, meuId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void solicitarMatch_semRequester_deveLancarAcessoNegado() {
        RoommateMatchRequestDTO dto = RoommateMatchRequestDTO.builder().destinatarioId(UUID.randomUUID()).build();

        assertThatThrownBy(() -> service.solicitarMatch(dto, null))
                .isInstanceOf(AcessoNegadoException.class);
    }

    // --- responderMatch (T5.8.5 / RF-34/RF-35) ----------------------------

    @Test
    void responderMatch_quandoDestinatarioAceita_deveAtualizarENotificar() {
        UUID matchId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        RoommateMatch match = RoommateMatch.builder()
                .id(matchId)
                .solicitanteId(solicitanteId)
                .destinatarioId(meuId)
                .status(RoommateMatch.Status.PENDENTE)
                .build();

        when(roommateMatchRepository.findById(matchId)).thenReturn(Optional.of(match));

        RoommateMatchStatusUpdateDTO dto = RoommateMatchStatusUpdateDTO.builder().status("ACEITO").build();

        RoommateMatchResponseDTO resultado = service.responderMatch(matchId, dto, meuId);

        assertThat(resultado.getStatus()).isEqualTo(RoommateMatch.Status.ACEITO.name());
        verify(roommateMatchRepository).save(match);
        verify(notificacaoRepository).save(any(Notificacao.class));
    }

    @Test
    void responderMatch_quandoNaoEhDestinatario_deveLancarAcessoNegado() {
        UUID matchId = UUID.randomUUID();
        RoommateMatch match = RoommateMatch.builder()
                .id(matchId)
                .solicitanteId(UUID.randomUUID())
                .destinatarioId(UUID.randomUUID())
                .status(RoommateMatch.Status.PENDENTE)
                .build();

        when(roommateMatchRepository.findById(matchId)).thenReturn(Optional.of(match));

        RoommateMatchStatusUpdateDTO dto = RoommateMatchStatusUpdateDTO.builder().status("ACEITO").build();

        assertThatThrownBy(() -> service.responderMatch(matchId, dto, meuId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(roommateMatchRepository, never()).save(any());
        verify(notificacaoRepository, never()).save(any());
    }

    @Test
    void responderMatch_quandoJaRespondido_deveLancarStatusInvalido() {
        UUID matchId = UUID.randomUUID();
        RoommateMatch match = RoommateMatch.builder()
                .id(matchId)
                .solicitanteId(UUID.randomUUID())
                .destinatarioId(meuId)
                .status(RoommateMatch.Status.ACEITO)
                .build();

        when(roommateMatchRepository.findById(matchId)).thenReturn(Optional.of(match));

        RoommateMatchStatusUpdateDTO dto = RoommateMatchStatusUpdateDTO.builder().status("RECUSADO").build();

        assertThatThrownBy(() -> service.responderMatch(matchId, dto, meuId))
                .isInstanceOf(StatusMatchInvalidoException.class);
        verify(roommateMatchRepository, never()).save(any());
    }

    @Test
    void responderMatch_quandoStatusInvalido_deveLancarStatusInvalido() {
        UUID matchId = UUID.randomUUID();
        RoommateMatch match = RoommateMatch.builder()
                .id(matchId)
                .solicitanteId(UUID.randomUUID())
                .destinatarioId(meuId)
                .status(RoommateMatch.Status.PENDENTE)
                .build();

        when(roommateMatchRepository.findById(matchId)).thenReturn(Optional.of(match));

        RoommateMatchStatusUpdateDTO dto = RoommateMatchStatusUpdateDTO.builder().status("BANANA").build();

        assertThatThrownBy(() -> service.responderMatch(matchId, dto, meuId))
                .isInstanceOf(StatusMatchInvalidoException.class);
        verify(roommateMatchRepository, never()).save(any());
    }

    @Test
    void responderMatch_quandoMatchNaoExiste_deveLancarMatchNaoEncontrado() {
        UUID matchId = UUID.randomUUID();
        when(roommateMatchRepository.findById(matchId)).thenReturn(Optional.empty());

        RoommateMatchStatusUpdateDTO dto = RoommateMatchStatusUpdateDTO.builder().status("ACEITO").build();

        assertThatThrownBy(() -> service.responderMatch(matchId, dto, meuId))
                .isInstanceOf(MatchNaoEncontradoException.class);
    }
}
