package com.campusliving.service.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.campusliving.dto.avaliacao.AvaliacaoRequestDTO;
import com.campusliving.dto.avaliacao.AvaliacaoResponseDTO;
import com.campusliving.dto.avaliacao.RespostaLocadorRequestDTO;
import com.campusliving.exception.avaliacao.AutoAvaliacaoException;
import com.campusliving.exception.avaliacao.AvaliacaoDuplicadaException;
import com.campusliving.exception.avaliacao.AvaliacaoNaoEncontradaException;
import com.campusliving.exception.avaliacao.ComentarioImproprioException;
import com.campusliving.exception.avaliacao.VinculoNaoEncontradoException;
import com.campusliving.exception.interacao.AnuncioNaoEncontradoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.avaliacao.Avaliacao;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.avaliacao.AvaliacaoRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.interacao.ContatoRepository;
import com.campusliving.repository.usuario.UserRepository;

/** Testes unitários do T5.7 (RF-29 publicar, RF-31 responder, listagens). */
@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceImplTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ContatoRepository contatoRepository;
    @Mock
    private PalavraoFilter palavraoFilter;

    private AvaliacaoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AvaliacaoServiceImpl(
                avaliacaoRepository, anuncioRepository, userRepository, contatoRepository, palavraoFilter);
    }

    // ------------------------------------------------------------------
    // publicar() — RF-29
    // ------------------------------------------------------------------

    @Test
    void publicar_quandoTudoValido_deveCriarAvaliacaoComContatoPrevioTrue() {
        UUID avaliadorId = UUID.randomUUID();
        UUID locadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Otimo local, recomendo").build();

        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(locadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));
        when(contatoRepository.existeContatoEntre(avaliadorId, locadorId)).thenReturn(true);
        when(avaliacaoRepository.existsByAvaliadorIdAndAdId(avaliadorId, adId)).thenReturn(false);
        when(palavraoFilter.contemPalavraImpropria(dto.getComentario())).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoResponseDTO resultado = service.publicar(dto, avaliadorId);

        assertThat(resultado.getAvaliadorId()).isEqualTo(avaliadorId);
        assertThat(resultado.getAvaliadoId()).isEqualTo(locadorId);
        assertThat(resultado.getAdId()).isEqualTo(adId);
        assertThat(resultado.getNota()).isEqualTo((short) 5);
        assertThat(resultado.isContatoPrevio()).isTrue();
    }

    @Test
    void publicar_semAvaliadorAutenticado_deveLancarAcessoNegado() {
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(UUID.randomUUID()).nota(5).comentario("Bom").build();

        assertThatThrownBy(() -> service.publicar(dto, null))
                .isInstanceOf(AcessoNegadoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_quandoAvaliadorNaoExiste_deveLancarUserNotFound() {
        UUID avaliadorId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(UUID.randomUUID()).nota(5).comentario("Bom").build();

        when(userRepository.existsById(avaliadorId)).thenReturn(false);

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(UserNotFoundException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_quandoAnuncioNaoExiste_deveLancarAnuncioNaoEncontrado() {
        UUID avaliadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Bom").build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(AnuncioNaoEncontradoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_quandoAvaliadorEhOProprioLocador_deveLancarAutoAvaliacao() {
        UUID avaliadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Bom").build();

        // avaliador é o próprio dono do anúncio
        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(avaliadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(AutoAvaliacaoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_semVinculoPrevio_deveLancarVinculoNaoEncontrado() {
        UUID avaliadorId = UUID.randomUUID();
        UUID locadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Bom").build();

        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(locadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));
        when(contatoRepository.existeContatoEntre(avaliadorId, locadorId)).thenReturn(false);

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(VinculoNaoEncontradoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_quandoJaAvaliouOAnuncio_deveLancarAvaliacaoDuplicada() {
        UUID avaliadorId = UUID.randomUUID();
        UUID locadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Bom").build();

        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(locadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));
        when(contatoRepository.existeContatoEntre(avaliadorId, locadorId)).thenReturn(true);
        when(avaliacaoRepository.existsByAvaliadorIdAndAdId(avaliadorId, adId)).thenReturn(true);

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(AvaliacaoDuplicadaException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void publicar_quandoCorridaConcorrenteEstouraConstraintUnica_deveLancarAvaliacaoDuplicada() {
        // Duas requisições passam pela checagem existsBy ao mesmo tempo; quem
        // chega no INSERT depois esbarra no índice único parcial do banco.
        UUID avaliadorId = UUID.randomUUID();
        UUID locadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(5).comentario("Bom").build();

        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(locadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));
        when(contatoRepository.existeContatoEntre(avaliadorId, locadorId)).thenReturn(true);
        when(avaliacaoRepository.existsByAvaliadorIdAndAdId(avaliadorId, adId)).thenReturn(false);
        when(palavraoFilter.contemPalavraImpropria(dto.getComentario())).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenThrow(new DataIntegrityViolationException("uq_reviews_avaliador_ad"));

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(AvaliacaoDuplicadaException.class);
    }

    @Test
    void publicar_comComentarioImproprio_deveLancarComentarioImproprio() {
        UUID avaliadorId = UUID.randomUUID();
        UUID locadorId = UUID.randomUUID();
        UUID adId = UUID.randomUUID();
        AvaliacaoRequestDTO dto = AvaliacaoRequestDTO.builder()
                .adId(adId).nota(1).comentario("Comentario com palavrao").build();

        Anuncio anuncio = Anuncio.builder().id(adId).locadorId(locadorId).build();

        when(userRepository.existsById(avaliadorId)).thenReturn(true);
        when(anuncioRepository.findById(adId)).thenReturn(Optional.of(anuncio));
        when(contatoRepository.existeContatoEntre(avaliadorId, locadorId)).thenReturn(true);
        when(avaliacaoRepository.existsByAvaliadorIdAndAdId(avaliadorId, adId)).thenReturn(false);
        when(palavraoFilter.contemPalavraImpropria(dto.getComentario())).thenReturn(true);

        assertThatThrownBy(() -> service.publicar(dto, avaliadorId))
                .isInstanceOf(ComentarioImproprioException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // responder() — RF-31
    // ------------------------------------------------------------------

    @Test
    void responder_quandoLocadorEhDono_devePersistirResposta() {
        UUID locadorId = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();
        RespostaLocadorRequestDTO dto = RespostaLocadorRequestDTO.builder()
                .resposta("Obrigado pelo feedback!").build();

        Avaliacao avaliacao = Avaliacao.builder()
                .id(avaliacaoId).avaliadoId(locadorId).adId(UUID.randomUUID())
                .nota((short) 4).contatoPrevio(true).build();

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(avaliacao));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AvaliacaoResponseDTO resultado = service.responder(avaliacaoId, dto, locadorId);

        assertThat(resultado.getRespostaLocador()).isEqualTo("Obrigado pelo feedback!");
    }

    @Test
    void responder_semLocadorAutenticado_deveLancarAcessoNegado() {
        RespostaLocadorRequestDTO dto = RespostaLocadorRequestDTO.builder().resposta("Obrigado").build();

        assertThatThrownBy(() -> service.responder(UUID.randomUUID(), dto, null))
                .isInstanceOf(AcessoNegadoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void responder_quandoAvaliacaoNaoExiste_deveLancarAvaliacaoNaoEncontrada() {
        UUID avaliacaoId = UUID.randomUUID();
        RespostaLocadorRequestDTO dto = RespostaLocadorRequestDTO.builder().resposta("Obrigado").build();

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.responder(avaliacaoId, dto, UUID.randomUUID()))
                .isInstanceOf(AvaliacaoNaoEncontradaException.class);
    }

    @Test
    void responder_quandoLocadorNaoEhDonoDoAnuncioAvaliado_deveLancarAcessoNegado() {
        UUID donoReal = UUID.randomUUID();
        UUID outroLocador = UUID.randomUUID();
        UUID avaliacaoId = UUID.randomUUID();
        RespostaLocadorRequestDTO dto = RespostaLocadorRequestDTO.builder().resposta("Obrigado").build();

        Avaliacao avaliacao = Avaliacao.builder()
                .id(avaliacaoId).avaliadoId(donoReal).adId(UUID.randomUUID())
                .nota((short) 4).contatoPrevio(true).build();

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(avaliacao));

        assertThatThrownBy(() -> service.responder(avaliacaoId, dto, outroLocador))
                .isInstanceOf(AcessoNegadoException.class);
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void responder_quandoAvaliadoIdEstaAnonimizado_deveLancarAcessoNegado() {
        // RNF/LEG-02: locador excluiu a conta, avaliado_id virou NULL — não
        // há mais ninguém autorizado a responder essa avaliação.
        UUID avaliacaoId = UUID.randomUUID();
        RespostaLocadorRequestDTO dto = RespostaLocadorRequestDTO.builder().resposta("Obrigado").build();

        Avaliacao avaliacao = Avaliacao.builder()
                .id(avaliacaoId).avaliadoId(null).adId(UUID.randomUUID())
                .nota((short) 4).contatoPrevio(true).build();

        when(avaliacaoRepository.findById(avaliacaoId)).thenReturn(Optional.of(avaliacao));

        assertThatThrownBy(() -> service.responder(avaliacaoId, dto, UUID.randomUUID()))
                .isInstanceOf(AcessoNegadoException.class);
    }

    // ------------------------------------------------------------------
    // listarPorAnuncio() / listarPorLocador()
    // ------------------------------------------------------------------

    @Test
    void listarPorAnuncio_quandoAnuncioNaoExiste_deveLancarAnuncioNaoEncontrado() {
        UUID adId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(anuncioRepository.existsById(adId)).thenReturn(false);

        assertThatThrownBy(() -> service.listarPorAnuncio(adId, pageable))
                .isInstanceOf(AnuncioNaoEncontradoException.class);
    }

    @Test
    void listarPorAnuncio_quandoExiste_devePaginarAvaliacoes() {
        UUID adId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        Avaliacao avaliacao = Avaliacao.builder()
                .id(UUID.randomUUID()).adId(adId).nota((short) 4).contatoPrevio(true).build();
        Page<Avaliacao> pagina = new PageImpl<>(java.util.List.of(avaliacao), pageable, 1);

        when(anuncioRepository.existsById(adId)).thenReturn(true);
        when(avaliacaoRepository.findByAdId(adId, pageable)).thenReturn(pagina);

        Page<AvaliacaoResponseDTO> resultado = service.listarPorAnuncio(adId, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getAdId()).isEqualTo(adId);
    }

    @Test
    void listarPorLocador_quandoLocadorNaoExiste_deveLancarUserNotFound() {
        UUID locadorId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        when(userRepository.existsById(locadorId)).thenReturn(false);

        assertThatThrownBy(() -> service.listarPorLocador(locadorId, pageable))
                .isInstanceOf(UserNotFoundException.class);
    }
}