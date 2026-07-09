package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.campusliving.dto.imovel.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.imovel.AnuncioRequestDTO;
import com.campusliving.dto.imovel.AnuncioResponseDTO;
import com.campusliving.dto.imovel.AnuncioStatusUpdateDTO;
import com.campusliving.dto.imovel.AnuncioUpdateRequestDTO;
import com.campusliving.dto.imovel.RegrasCasaRequestDTO;
import com.campusliving.dto.imovel.VisualizacaoPorDiaDTO;
import com.campusliving.exception.imovel.AnuncioDuplicadoException;
import com.campusliving.exception.imovel.AnuncioNaoEncontradoException;
import com.campusliving.exception.imovel.ImovelNaoEncontradoException;
import com.campusliving.exception.imovel.StatusAnuncioInvalidoException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.ImagemAnuncio;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.imovel.RegrasCasa;
import com.campusliving.model.imovel.VisualizacaoAnuncio;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.avaliacao.AvaliacaoRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImagemAnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.imovel.RegrasCasaRepository;
import com.campusliving.repository.imovel.VisualizacaoAnuncioRepository;
import com.campusliving.repository.usuario.UserRepository;

@ExtendWith(MockitoExtension.class)
class AnuncioServiceImplTest {

    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private ImovelRepository imovelRepository;
    @Mock
    private RegrasCasaRepository regrasCasaRepository;
    @Mock
    private ImagemAnuncioRepository imagemAnuncioRepository;
    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @Mock
    private VisualizacaoAnuncioRepository visualizacaoAnuncioRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnuncioGeoService anuncioGeoService;

    private AnuncioServiceImpl service;

    private UUID imovelId;
    private UUID anuncioId;
    private UUID locadorId;
    private UUID estranhoId;
    private Imovel imovel;
    private User estranho;

    @BeforeEach
    void setUp() {
        service = new AnuncioServiceImpl(anuncioRepository, imovelRepository, regrasCasaRepository,
                imagemAnuncioRepository, avaliacaoRepository, visualizacaoAnuncioRepository, userRepository,
                anuncioGeoService);

        imovelId = UUID.randomUUID();
        anuncioId = UUID.randomUUID();
        locadorId = UUID.randomUUID();
        estranhoId = UUID.randomUUID();

        imovel = Imovel.builder().id(imovelId).proprietarioId(locadorId).build();
        estranho = User.builder().id(estranhoId).nome("Estranho").tipoConta(User.Tipo.ESTUDANTE).build();
    }

    private Anuncio anuncioAtivo() {
        return Anuncio.builder()
                .id(anuncioId)
                .imovelId(imovelId)
                .locadorId(locadorId)
                .titulo("Quarto perto da UFCG")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("500.00"))
                .precoCondominio(BigDecimal.ZERO)
                .precoIptu(BigDecimal.ZERO)
                .status(Anuncio.Status.ATIVO)
                .vagasTotal(1)
                .vagasDisponiveis(1)
                .visualizacoes(5)
                .build();
    }

    // --- publicar (T5.5.2 / RF-12) -----------------------------------------

    @Test
    void publicar_quandoValido_deveCriarAtivoEDispararJobGeo() {
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));
        when(anuncioRepository.existsByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(false);

        AnuncioRequestDTO dto = AnuncioRequestDTO.builder()
                .imovelId(imovelId)
                .titulo("Vaga compartilhada")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("450.00"))
                .build();

        AnuncioResponseDTO resultado = service.publicar(dto, locadorId);

        assertThat(resultado.getStatus()).isEqualTo(Anuncio.Status.ATIVO.name());
        assertThat(resultado.getLocadorId()).isEqualTo(locadorId);
        assertThat(resultado.getVagasTotal()).isEqualTo(1);
        assertThat(resultado.getVagasDisponiveis()).isEqualTo(1);
        verify(anuncioRepository).save(any(Anuncio.class));
        verify(anuncioGeoService).calcularDistanciaUfcg(any(UUID.class));
        verify(regrasCasaRepository, never()).save(any());
    }

    @Test
    void publicar_quandoNaoEhDonoDoImovel_deveLancarAcessoNegado() {
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));

        AnuncioRequestDTO dto = AnuncioRequestDTO.builder()
                .imovelId(imovelId)
                .titulo("Vaga")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("450.00"))
                .build();

        assertThatThrownBy(() -> service.publicar(dto, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(anuncioRepository, never()).existsByImovelIdAndStatus(any(), any());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicar_quandoImovelNaoExiste_deveLancarImovelNaoEncontrado() {
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.empty());

        AnuncioRequestDTO dto = AnuncioRequestDTO.builder()
                .imovelId(imovelId)
                .titulo("Vaga")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("450.00"))
                .build();

        assertThatThrownBy(() -> service.publicar(dto, locadorId))
                .isInstanceOf(ImovelNaoEncontradoException.class);
    }

    @Test
    void publicar_quandoJaExisteAtivo_deveLancarAnuncioDuplicado() {
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));
        when(anuncioRepository.existsByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(true);

        AnuncioRequestDTO dto = AnuncioRequestDTO.builder()
                .imovelId(imovelId)
                .titulo("Vaga")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("450.00"))
                .build();

        assertThatThrownBy(() -> service.publicar(dto, locadorId))
                .isInstanceOf(AnuncioDuplicadoException.class);
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicar_comRegrasCasa_deveSalvarRegras() {
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));
        when(anuncioRepository.existsByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(false);

        AnuncioRequestDTO dto = AnuncioRequestDTO.builder()
                .imovelId(imovelId)
                .titulo("Vaga")
                .tipoOferta(Anuncio.TipoOferta.VAGA_COMPARTILHADA)
                .precoAluguel(new BigDecimal("450.00"))
                .regrasCasa(RegrasCasaRequestDTO.builder().petFriendly(true).build())
                .build();

        service.publicar(dto, locadorId);

        verify(regrasCasaRepository).save(any(RegrasCasa.class));
    }

    // --- atualizar (T5.5.3 / RF-13) -----------------------------------------

    @Test
    void atualizar_quandoDono_deveAtualizarCamposEnviados() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));

        AnuncioUpdateRequestDTO dto = AnuncioUpdateRequestDTO.builder()
                .precoAluguel(new BigDecimal("600.00"))
                .descricao("Nova descricao")
                .build();

        AnuncioResponseDTO resultado = service.atualizar(anuncioId, dto, locadorId);

        assertThat(resultado.getPrecoAluguel()).isEqualByComparingTo("600.00");
        assertThat(resultado.getDescricao()).isEqualTo("Nova descricao");
        verify(anuncioRepository).save(anuncio);
        verify(regrasCasaRepository, never()).save(any());
    }

    @Test
    void atualizar_quandoNaoEhDonoNemAdmin_deveLancarAcessoNegado() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));

        AnuncioUpdateRequestDTO dto = AnuncioUpdateRequestDTO.builder().descricao("x").build();

        assertThatThrownBy(() -> service.atualizar(anuncioId, dto, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void atualizar_quandoAnuncioNaoExiste_deveLancarAnuncioNaoEncontrado() {
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.empty());

        AnuncioUpdateRequestDTO dto = AnuncioUpdateRequestDTO.builder().descricao("x").build();

        assertThatThrownBy(() -> service.atualizar(anuncioId, dto, locadorId))
                .isInstanceOf(AnuncioNaoEncontradoException.class);
    }

    @Test
    void atualizar_comCamposDeRegras_deveCriarRegrasCasaSeNaoExistir() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(regrasCasaRepository.findById(anuncioId)).thenReturn(Optional.empty());

        AnuncioUpdateRequestDTO dto = AnuncioUpdateRequestDTO.builder().petFriendly(true).build();

        service.atualizar(anuncioId, dto, locadorId);

        verify(regrasCasaRepository).save(any(RegrasCasa.class));
    }

    // --- atualizarStatus (T5.5.4 / RF-14) -----------------------------------

    @Test
    void atualizarStatus_paraInativo_deveAtualizar() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));

        AnuncioStatusUpdateDTO dto = AnuncioStatusUpdateDTO.builder().status("INATIVO").build();

        AnuncioResponseDTO resultado = service.atualizarStatus(anuncioId, dto, locadorId);

        assertThat(resultado.getStatus()).isEqualTo(Anuncio.Status.INATIVO.name());
        verify(anuncioRepository).save(anuncio);
        verify(anuncioRepository, never()).existsByImovelIdAndStatusAndIdNot(any(), any(), any());
    }

    @Test
    void atualizarStatus_reativarComOutroAtivoExistente_deveLancarDuplicado() {
        Anuncio anuncio = anuncioAtivo();
        anuncio.setStatus(Anuncio.Status.INATIVO);
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(anuncioRepository.existsByImovelIdAndStatusAndIdNot(imovelId, Anuncio.Status.ATIVO, anuncioId))
                .thenReturn(true);

        AnuncioStatusUpdateDTO dto = AnuncioStatusUpdateDTO.builder().status("ATIVO").build();

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, dto, locadorId))
                .isInstanceOf(AnuncioDuplicadoException.class);
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_valorInvalido_deveLancarStatusInvalido() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));

        AnuncioStatusUpdateDTO dto = AnuncioStatusUpdateDTO.builder().status("BANANA").build();

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, dto, locadorId))
                .isInstanceOf(StatusAnuncioInvalidoException.class);
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_valorAlugado_deveLancarStatusInvalido() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));

        AnuncioStatusUpdateDTO dto = AnuncioStatusUpdateDTO.builder().status("ALUGADO").build();

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, dto, locadorId))
                .isInstanceOf(StatusAnuncioInvalidoException.class);
    }

    // --- getDetalhes (T5.5.5 / RF-15) ---------------------------------------

    @Test
    void getDetalhes_quandoNaoEhDono_deveIncrementarVisualizacoes() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imagemAnuncioRepository.findByAdIdOrderByOrdemAsc(anuncioId))
                .thenReturn(List.of(ImagemAnuncio.builder().url("http://x/1.jpg").build()));
        when(avaliacaoRepository.calcularNotaMedia(anuncioId)).thenReturn(4.5);

        AnuncioDetalhesResponseDTO resultado = service.getDetalhes(anuncioId, estranhoId);

        assertThat(resultado.getAnuncio().getVisualizacoes()).isEqualTo(6);
        assertThat(resultado.getImagensUrls()).containsExactly("http://x/1.jpg");
        assertThat(resultado.getNotaMedia()).isEqualTo(4.5);
        verify(visualizacaoAnuncioRepository).registrarVisualizacao(anuncioId);
        verify(anuncioRepository).save(anuncio);
    }

    @Test
    void getDetalhes_quandoEhDono_naoDeveIncrementarVisualizacoes() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imagemAnuncioRepository.findByAdIdOrderByOrdemAsc(anuncioId)).thenReturn(List.of());
        when(avaliacaoRepository.calcularNotaMedia(anuncioId)).thenReturn(null);

        AnuncioDetalhesResponseDTO resultado = service.getDetalhes(anuncioId, locadorId);

        assertThat(resultado.getAnuncio().getVisualizacoes()).isEqualTo(5);
        assertThat(resultado.getNotaMedia()).isNull();
        verify(visualizacaoAnuncioRepository, never()).registrarVisualizacao(any());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void getDetalhes_semRequester_deveIncrementarComoVisitanteAnonimo() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imagemAnuncioRepository.findByAdIdOrderByOrdemAsc(anuncioId)).thenReturn(List.of());
        when(avaliacaoRepository.calcularNotaMedia(anuncioId)).thenReturn(null);

        service.getDetalhes(anuncioId, null);

        verify(visualizacaoAnuncioRepository).registrarVisualizacao(anuncioId);
        verify(anuncioRepository).save(anuncio);
    }

    // --- getEstatisticas (T5.5.7 / RF-17) -----------------------------------

    @Test
    void getEstatisticas_quandoDono_deveRetornarLista() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(visualizacaoAnuncioRepository.findByAdIdOrderByDataVisualizacaoAsc(anuncioId)).thenReturn(List.of(
                VisualizacaoAnuncio.builder().adId(anuncioId).dataVisualizacao(LocalDate.of(2026, 7, 1)).quantidade(3).build(),
                VisualizacaoAnuncio.builder().adId(anuncioId).dataVisualizacao(LocalDate.of(2026, 7, 2)).quantidade(5).build()
        ));

        List<VisualizacaoPorDiaDTO> resultado = service.getEstatisticas(anuncioId, locadorId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getData()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(resultado.get(0).getQuantidade()).isEqualTo(3);
        assertThat(resultado.get(1).getQuantidade()).isEqualTo(5);
    }

    @Test
    void getEstatisticas_quandoNaoEhDonoNemAdmin_deveLancarAcessoNegado() {
        Anuncio anuncio = anuncioAtivo();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(userRepository.findById(estranhoId)).thenReturn(Optional.of(estranho));

        assertThatThrownBy(() -> service.getEstatisticas(anuncioId, estranhoId))
                .isInstanceOf(AcessoNegadoException.class);
        verify(visualizacaoAnuncioRepository, never()).findByAdIdOrderByDataVisualizacaoAsc(any());
    }
}
