package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.campusliving.dto.anuncio.AnuncioDetalhesResponseDTO;
import com.campusliving.dto.anuncio.AnuncioEstatisticasResponseDTO;
import com.campusliving.dto.anuncio.AnuncioPaginadoResponseDTO;
import com.campusliving.dto.anuncio.AnuncioRequestDTO;
import com.campusliving.dto.anuncio.AnuncioResponseDTO;
import com.campusliving.dto.anuncio.AnuncioUpdateRequestDTO;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;

/** Testes unitários dos fluxos de publicação, gestão e busca de anúncios. */
@ExtendWith(MockitoExtension.class)
class AnuncioServiceTest {

    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private ImovelRepository imovelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private ImagemAnuncioService imagemAnuncioService;
    @Mock
    private com.campusliving.repository.avaliacao.AvaliacaoRepository avaliacaoRepository;

    private AnuncioService service;

    @BeforeEach
    void setUp() {
        service = new AnuncioService(anuncioRepository, imovelRepository, userRepository,
                auditLogService, imagemAnuncioService, avaliacaoRepository);
    }

    // ------------------------------------------------------------------
    // publicarAnuncio() — RF-12 / RF-16
    // ------------------------------------------------------------------

    @Test
    void publicarAnuncio_comDadosValidos_calculaDistanciaETemposRegistraAuditoria() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        AnuncioRequestDTO request = requestPara(imovelId);

        when(userRepository.findByEmail("locador@campusliving.com"))
                .thenReturn(List.of(usuario(locadorId, User.Tipo.LOCADOR)));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel(imovelId, locadorId)));
        when(anuncioRepository.findByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(List.of());
        when(anuncioRepository.calcularDistanciaUfcgMetros(eq(imovelId), anyDouble(), anyDouble())).thenReturn(830);
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(invocation -> {
            Anuncio anuncio = invocation.getArgument(0);
            anuncio.setId(anuncioId);
            return anuncio;
        });

        AnuncioResponseDTO resultado = service.publicarAnuncio(request, "locador@campusliving.com");

        ArgumentCaptor<Anuncio> anuncioCaptor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(anuncioCaptor.capture());
        Anuncio salvo = anuncioCaptor.getValue();
        assertThat(salvo.getImovelId()).isEqualTo(imovelId);
        assertThat(salvo.getLocadorId()).isEqualTo(locadorId);
        assertThat(salvo.getStatus()).isEqualTo(Anuncio.Status.ATIVO);
        assertThat(salvo.getDistanciaUfcgMetros()).isEqualTo(830);
        assertThat(salvo.getTempoPeMin()).isEqualTo(10);
        assertThat(salvo.getTempoOnibusMin()).isEqualTo(3);
        assertThat(salvo.isGeoFallback()).isFalse();
        assertThat(resultado.getId()).isEqualTo(anuncioId);
        assertThat(resultado.getMensagem()).isEqualTo("Anúncio publicado com sucesso!");
        verify(auditLogService).registrarAcao(locadorId, "PUBLICAR_ANUNCIO", "Anuncio", anuncioId);
    }

    @Test
    void publicarAnuncio_semGeometriaDoImovel_usaFallbackSemTemposDeDeslocamento() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();

        prepararPublicacaoValida(locadorId, imovelId);
        when(anuncioRepository.calcularDistanciaUfcgMetros(eq(imovelId), anyDouble(), anyDouble())).thenReturn(null);
        when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.publicarAnuncio(requestPara(imovelId), "locador@campusliving.com");

        ArgumentCaptor<Anuncio> anuncioCaptor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(anuncioCaptor.capture());
        Anuncio salvo = anuncioCaptor.getValue();
        assertThat(salvo.getDistanciaUfcgMetros()).isNull();
        assertThat(salvo.getTempoPeMin()).isNull();
        assertThat(salvo.getTempoOnibusMin()).isNull();
        assertThat(salvo.isGeoFallback()).isTrue();
    }

    @Test
    void publicarAnuncio_quandoLocadorNaoExiste_rejeitaSemPersistir() {
        UUID imovelId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of());

        assertThatThrownBy(() -> service.publicarAnuncio(requestPara(imovelId), "ausente@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Locador não encontrado");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicarAnuncio_quandoUsuarioNaoEhLocadorNemAdmin_rejeitaPublicacao() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(locadorId, User.Tipo.ESTUDANTE)));

        assertThatThrownBy(() -> service.publicarAnuncio(requestPara(imovelId), "estudante@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Apenas LOCADOR, MISTO ou ADMIN podem publicar anúncios");

        verify(imovelRepository, never()).findById(any());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicarAnuncio_quandoImovelNaoExiste_rejeitaPublicacao() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(locadorId, User.Tipo.LOCADOR)));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publicarAnuncio(requestPara(imovelId), "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicarAnuncio_quandoImovelPertenceAOutroLocador_rejeitaPublicacao() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        prepararLocadorEImovel(locadorId, imovelId, UUID.randomUUID());

        assertThatThrownBy(() -> service.publicarAnuncio(requestPara(imovelId), "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Este imóvel não pertence ao locador informado");

        verify(anuncioRepository, never()).findByImovelIdAndStatus(any(), any());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void publicarAnuncio_quandoJaExisteAnuncioAtivo_rejeitaDuplicidade() {
        UUID locadorId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        prepararLocadorEImovel(locadorId, imovelId, locadorId);
        when(anuncioRepository.findByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO))
                .thenReturn(List.of(anuncio(UUID.randomUUID(), imovelId, locadorId)));

        assertThatThrownBy(() -> service.publicarAnuncio(requestPara(imovelId), "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Já existe um anúncio ativo para este imóvel");

        verify(anuncioRepository, never()).calcularDistanciaUfcgMetros(any(), anyDouble(), anyDouble());
        verify(anuncioRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // atualizarStatus() / editarAnuncio() — RF-13 / RF-14
    // ------------------------------------------------------------------

    @Test
    void atualizarStatus_quandoDonoInativaAnuncio_persisteNovoStatusERegistraAuditoria() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        Anuncio anuncio = anuncio(anuncioId, UUID.randomUUID(), locadorId);
        prepararUsuarioEAnuncio(locadorId, User.Tipo.LOCADOR, anuncio);
        when(anuncioRepository.save(anuncio)).thenReturn(anuncio);

        AnuncioResponseDTO resultado = service.atualizarStatus(anuncioId, "INATIVO", "locador@campusliving.com");

        assertThat(anuncio.getStatus()).isEqualTo(Anuncio.Status.INATIVO);
        assertThat(resultado.getStatus()).isEqualTo("INATIVO");
        verify(anuncioRepository).save(anuncio);
        verify(auditLogService).registrarAcao(locadorId, "ATUALIZAR_STATUS_ANUNCIO", "Anuncio", anuncioId);
    }

    @Test
    void atualizarStatus_quandoAdminNaoEDono_podeAlterarAnuncio() {
        UUID adminId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        Anuncio anuncio = anuncio(anuncioId, UUID.randomUUID(), UUID.randomUUID());
        prepararUsuarioEAnuncio(adminId, User.Tipo.ADMIN, anuncio);
        when(anuncioRepository.save(anuncio)).thenReturn(anuncio);

        service.atualizarStatus(anuncioId, "ALUGADO", "admin@campusliving.com");

        assertThat(anuncio.getStatus()).isEqualTo(Anuncio.Status.ALUGADO);
        verify(auditLogService).registrarAcao(adminId, "ATUALIZAR_STATUS_ANUNCIO", "Anuncio", anuncioId);
    }

    @Test
    void atualizarStatus_quandoLocadorNaoExiste_rejeitaSemConsultarAnuncio() {
        UUID anuncioId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of());

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, "ATIVO", "ausente@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Locador não encontrado");

        verify(anuncioRepository, never()).findById(any());
    }

    @Test
    void atualizarStatus_quandoAnuncioNaoExiste_rejeitaAlteracao() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(locadorId, User.Tipo.LOCADOR)));
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, "ATIVO", "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Anúncio não encontrado");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_quandoUsuarioNaoEDonoNemAdmin_rejeitaAlteracao() {
        UUID donoId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        prepararUsuarioEAnuncio(outroId, User.Tipo.LOCADOR, anuncio(anuncioId, UUID.randomUUID(), donoId));

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, "INATIVO", "outro@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Você não tem permissão para alterar este anúncio");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void atualizarStatus_quandoStatusEhInvalido_rejeitaSemSalvar() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        prepararUsuarioEAnuncio(locadorId, User.Tipo.LOCADOR,
                anuncio(anuncioId, UUID.randomUUID(), locadorId));

        assertThatThrownBy(() -> service.atualizarStatus(anuncioId, "RASCUNHO", "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Status inválido. Valores permitidos: ATIVO, INATIVO, ALUGADO");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void editarAnuncio_quandoDonoEdita_persisteTodosOsCamposERegistraAuditoria() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        Anuncio anuncio = anuncio(anuncioId, UUID.randomUUID(), locadorId);
        AnuncioUpdateRequestDTO request = atualizacao();
        prepararUsuarioEAnuncio(locadorId, User.Tipo.LOCADOR, anuncio);
        when(anuncioRepository.save(anuncio)).thenReturn(anuncio);

        AnuncioResponseDTO resultado = service.editarAnuncio(anuncioId, request, "locador@campusliving.com");

        assertThat(anuncio.getTitulo()).isEqualTo(request.getTitulo());
        assertThat(anuncio.getDescricao()).isEqualTo(request.getDescricao());
        assertThat(anuncio.getTipoOferta()).isEqualTo(Anuncio.TipoOferta.VAGA_COMPARTILHADA);
        assertThat(anuncio.getPrecoAluguel()).isEqualByComparingTo("950.00");
        assertThat(anuncio.getVagasTotal()).isEqualTo(2);
        assertThat(anuncio.getVagasDisponiveis()).isEqualTo(1);
        assertThat(resultado.getTitulo()).isEqualTo(request.getTitulo());
        verify(auditLogService).registrarAcao(locadorId, "EDITAR_ANUNCIO", "Anuncio", anuncioId);
    }

    @Test
    void editarAnuncio_quandoUsuarioNaoTemPermissao_rejeitaSemSalvar() {
        UUID donoId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        prepararUsuarioEAnuncio(outroId, User.Tipo.LOCADOR, anuncio(anuncioId, UUID.randomUUID(), donoId));

        assertThatThrownBy(() -> service.editarAnuncio(anuncioId, atualizacao(), "outro@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Você não tem permissão para editar este anúncio");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void editarAnuncio_quandoAnuncioNaoExiste_rejeitaEdicao() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(locadorId, User.Tipo.LOCADOR)));
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editarAnuncio(anuncioId, atualizacao(), "locador@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Anúncio não encontrado");

        verify(anuncioRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // Detalhes, estatísticas e mapa — RF-15 / RF-16
    // ------------------------------------------------------------------

    @Test
    void buscarDetalhes_retornaDadosDoAnuncioEImovelEIncrementaVisualizacoes() {
        UUID anuncioId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        Anuncio anuncio = anuncio(anuncioId, imovelId, UUID.randomUUID());
        anuncio.setVisualizacoes(9);
        anuncio.setDistanciaUfcgMetros(1200);
        anuncio.setTempoPeMin(14);
        anuncio.setTempoOnibusMin(4);
        Imovel imovel = imovel(imovelId, anuncio.getLocadorId());
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));
        when(anuncioRepository.save(anuncio)).thenReturn(anuncio);
        // Cenário sem avaliações: média nula e contagem zero.
        when(avaliacaoRepository.mediaNotaPorAnuncio(anuncioId)).thenReturn(null);
        when(avaliacaoRepository.countByAdId(anuncioId)).thenReturn(0L);

        AnuncioDetalhesResponseDTO resultado = service.buscarDetalhes(anuncioId);

        assertThat(resultado.getId()).isEqualTo(anuncioId);
        assertThat(resultado.getImovelId()).isEqualTo(imovelId);
        assertThat(resultado.getTipoImovel()).isEqualTo("APARTAMENTO");
        assertThat(resultado.getVisualizacoes()).isEqualTo(10);
        assertThat(resultado.getDistanciaUfcgMetros()).isEqualTo(1200);
        assertThat(resultado.getTempoPeMin()).isEqualTo(14);
        assertThat(resultado.getImagens()).isEmpty();
        assertThat(resultado.getNotaMedia()).isNull();
        assertThat(resultado.getTotalAvaliacoes()).isZero();
        verify(anuncioRepository).save(anuncio);
    }

    @Test
    void buscarDetalhes_quandoAnuncioNaoExiste_lancaErro() {
        UUID anuncioId = UUID.randomUUID();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarDetalhes(anuncioId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Anúncio não encontrado");
    }

    @Test
    void buscarDetalhes_quandoImovelNaoExiste_lancaErroSemIncrementarVisualizacoes() {
        UUID anuncioId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        when(anuncioRepository.findById(anuncioId))
                .thenReturn(Optional.of(anuncio(anuncioId, imovelId, UUID.randomUUID())));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarDetalhes(anuncioId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado");

        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void buscarEstatisticas_quandoDonoConsulta_retornaVisualizacoesEHistoricoVazio() {
        UUID locadorId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        Anuncio anuncio = anuncio(anuncioId, UUID.randomUUID(), locadorId);
        anuncio.setVisualizacoes(42);
        prepararUsuarioEAnuncio(locadorId, User.Tipo.LOCADOR, anuncio);

        AnuncioEstatisticasResponseDTO resultado = service.buscarEstatisticas(anuncioId, "locador@campusliving.com");

        assertThat(resultado.getTotalVisualizacoes()).isEqualTo(42L);
        assertThat(resultado.getVisualizacoesPorDia()).isEmpty();
    }

    @Test
    void buscarEstatisticas_quandoUsuarioNaoEhDonoNemAdmin_rejeitaConsulta() {
        UUID donoId = UUID.randomUUID();
        UUID outroId = UUID.randomUUID();
        UUID anuncioId = UUID.randomUUID();
        prepararUsuarioEAnuncio(outroId, User.Tipo.LOCADOR, anuncio(anuncioId, UUID.randomUUID(), donoId));

        assertThatThrownBy(() -> service.buscarEstatisticas(anuncioId, "outro@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Você não tem permissão para ver as estatísticas deste anúncio");
    }

    @Test
    void buscarAnunciosParaMapa_converteTodosOsAnunciosAtivos() {
        UUID primeiroImovelId = UUID.randomUUID();
        UUID segundoImovelId = UUID.randomUUID();
        Anuncio primeiro = anuncio(UUID.randomUUID(), primeiroImovelId, UUID.randomUUID());
        Anuncio segundo = anuncio(UUID.randomUUID(), segundoImovelId, UUID.randomUUID());
        Imovel primeiroImovel = imovel(primeiroImovelId, primeiro.getLocadorId());
        Imovel segundoImovel = imovel(segundoImovelId, segundo.getLocadorId());
        segundoImovel.setTipo(Imovel.Tipo.QUARTO);
        segundoImovel.setLatitude(-7.3);
        segundoImovel.setLongitude(-35.9);
        when(anuncioRepository.findByStatus(Anuncio.Status.ATIVO)).thenReturn(List.of(primeiro, segundo));
        when(imovelRepository.findById(primeiroImovelId)).thenReturn(Optional.of(primeiroImovel));
        when(imovelRepository.findById(segundoImovelId)).thenReturn(Optional.of(segundoImovel));

        var resultado = service.buscarAnunciosParaMapa();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(primeiro.getId());
        assertThat(resultado.get(0).getTipo()).isEqualTo("APARTAMENTO");
        assertThat(resultado.get(1).getLatitude()).isEqualTo(-7.3);
        assertThat(resultado.get(1).getTipo()).isEqualTo("QUARTO");
    }

    @Test
    void buscarAnunciosParaMapa_quandoImovelDoAnuncioNaoExiste_lancaErro() {
        UUID imovelId = UUID.randomUUID();
        when(anuncioRepository.findByStatus(Anuncio.Status.ATIVO))
                .thenReturn(List.of(anuncio(UUID.randomUUID(), imovelId, UUID.randomUUID())));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarAnunciosParaMapa())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Imóvel não encontrado");
    }

    // ------------------------------------------------------------------
    // Paginação, ordenação, filtros e busca textual — RF-21 a RF-24
    // ------------------------------------------------------------------

    @Test
    void buscarAnunciosPaginados_normalizaParametrosInvalidosEConverteItens() {
        Anuncio anuncio = anuncio(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Page<Anuncio> pagina = new PageImpl<>(List.of(anuncio), PageRequest.of(0, 10), 21);
        when(anuncioRepository.findByStatus(eq(Anuncio.Status.ATIVO), any(Pageable.class))).thenReturn(pagina);

        AnuncioPaginadoResponseDTO resultado = service.buscarAnunciosPaginados(-1, 0);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(anuncioRepository).findByStatus(eq(Anuncio.Status.ATIVO), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isZero();
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
        assertThat(resultado.getTotalItems()).isEqualTo(21L);
        assertThat(resultado.getTotalPages()).isEqualTo(3);
        assertThat(resultado.getItems()).extracting(AnuncioResponseDTO::getId).containsExactly(anuncio.getId());
    }

    @Test
    void buscarAnunciosPaginadosComSort_limitaPaginaEUsaOrdenacaoSolicitada() {
        Page<Anuncio> pagina = new PageImpl<>(List.of(), PageRequest.of(2, 100), 0);
        when(anuncioRepository.findByStatus(eq(Anuncio.Status.ATIVO), any(Pageable.class))).thenReturn(pagina);

        AnuncioPaginadoResponseDTO resultado = service.buscarAnunciosPaginadosComSort(2, 101, "preco_asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(anuncioRepository).findByStatus(eq(Anuncio.Status.ATIVO), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(100);
        assertThat(pageable.getSort().getOrderFor("precoAluguel").getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(resultado.getLimit()).isEqualTo(100);
    }

    @Test
    void buscarAnunciosComFiltros_encaminhaTodosOsFiltrosAoRepositorio() {
        BigDecimal precoMax = new BigDecimal("1500.00");
        Anuncio anuncio = anuncio(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Page<Anuncio> pagina = new PageImpl<>(List.of(anuncio), PageRequest.of(1, 20), 1);
        when(anuncioRepository.findByFiltros(
                eq(Anuncio.Status.ATIVO), eq(precoMax), eq(2500), eq(true), eq(false), eq(false), eq(true),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Anuncio.TipoOferta.VAGA_COMPARTILHADA), eq(6), isNull(), any(Pageable.class))).thenReturn(pagina);

        AnuncioPaginadoResponseDTO resultado = service.buscarAnunciosComFiltros(
                1, 20, "distancia_asc", precoMax, 2500, true, false, false, true,
                null, null, null, null, null, null, "VAGA_COMPARTILHADA", 6);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(anuncioRepository).findByFiltros(
                eq(Anuncio.Status.ATIVO), eq(precoMax), eq(2500), eq(true), eq(false), eq(false), eq(true),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Anuncio.TipoOferta.VAGA_COMPARTILHADA), eq(6), isNull(), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("distanciaUfcgMetros").getDirection())
                .isEqualTo(Sort.Direction.ASC);
        assertThat(resultado.getItems()).hasSize(1);
    }

    @Test
    void buscarAnunciosComTexto_comConsultaNaoVazia_removeEspacosEUsaBuscaTextual() {
        Anuncio anuncio = anuncio(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Page<Anuncio> pagina = new PageImpl<>(List.of(anuncio), PageRequest.of(0, 10), 1);
        // Texto e filtros vão combinados na mesma query (findByFiltros), com o
        // termo de busca já sem espaços nas pontas no parâmetro `query`.
        when(anuncioRepository.findByFiltros(
                eq(Anuncio.Status.ATIVO), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq("perto da UFCG"), any(Pageable.class))).thenReturn(pagina);

        AnuncioPaginadoResponseDTO resultado = service.buscarAnunciosComTexto(
                0, 10, "preco_desc", "  perto da UFCG  ",
                null, null, null, null, null, null, // precoMax..incluiAlimentacao
                null, null, null, null, null, null, // seguranca24h..vagaGaragem
                null, null);                        // tipoOferta, mesesDesejados

        verify(anuncioRepository).findByFiltros(
                eq(Anuncio.Status.ATIVO), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq("perto da UFCG"), any(Pageable.class));
        assertThat(resultado.getItems()).hasSize(1);
    }

    @Test
    void buscarAnunciosComTexto_comConsultaEmBranco_usaFiltrosNormais() {
        Page<Anuncio> pagina = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(anuncioRepository.findByFiltros(
                eq(Anuncio.Status.ATIVO), eq(new BigDecimal("900.00")), eq(1000), eq(true), eq(true), eq(false),
                eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Anuncio.TipoOferta.IMOVEL_COMPLETO), isNull(), isNull(), any(Pageable.class))).thenReturn(pagina);

        AnuncioPaginadoResponseDTO resultado = service.buscarAnunciosComTexto(
                0, 10, null, "   ",
                new BigDecimal("900.00"), 1000, true, true, false, false, // precoMax..incluiAlimentacao
                null, null, null, null, null, null,                      // seguranca24h..vagaGaragem
                "IMOVEL_COMPLETO", null);                                // tipoOferta, mesesDesejados

        verify(anuncioRepository).findByFiltros(
                eq(Anuncio.Status.ATIVO), eq(new BigDecimal("900.00")), eq(1000), eq(true), eq(true), eq(false),
                eq(false), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(Anuncio.TipoOferta.IMOVEL_COMPLETO), isNull(), isNull(), any(Pageable.class));
        assertThat(resultado.getItems()).isEmpty();
    }

    private void prepararPublicacaoValida(UUID locadorId, UUID imovelId) {
        prepararLocadorEImovel(locadorId, imovelId, locadorId);
        when(anuncioRepository.findByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(List.of());
    }

    private void prepararLocadorEImovel(UUID locadorId, UUID imovelId, UUID proprietarioId) {
        when(userRepository.findByEmail("locador@campusliving.com"))
                .thenReturn(List.of(usuario(locadorId, User.Tipo.LOCADOR)));
        when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel(imovelId, proprietarioId)));
    }

    private void prepararUsuarioEAnuncio(UUID usuarioId, User.Tipo tipo, Anuncio anuncio) {
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(usuarioId, tipo)));
        when(anuncioRepository.findById(anuncio.getId())).thenReturn(Optional.of(anuncio));
    }

    private AnuncioRequestDTO requestPara(UUID imovelId) {
        AnuncioRequestDTO request = new AnuncioRequestDTO();
        request.setImovelId(imovelId.toString());
        request.setTitulo("Apartamento perto da UFCG");
        request.setTipoOferta("IMOVEL_COMPLETO");
        request.setPrecoAluguel(new BigDecimal("1200.00"));
        request.setPrecoCondominio(new BigDecimal("100.00"));
        request.setPrecoIptu(BigDecimal.ZERO);
        request.setDescricao("Dois quartos e internet inclusa");
        request.setVagasTotal(2);
        request.setVagasDisponiveis(2);
        return request;
    }

    private AnuncioUpdateRequestDTO atualizacao() {
        AnuncioUpdateRequestDTO request = new AnuncioUpdateRequestDTO();
        request.setTitulo("Quarto mobiliado atualizado");
        request.setDescricao("Valor com água inclusa");
        request.setTipoOferta("VAGA_COMPARTILHADA");
        request.setPrecoAluguel(new BigDecimal("950.00"));
        request.setPrecoCondominio(new BigDecimal("80.00"));
        request.setPrecoIptu(new BigDecimal("20.00"));
        request.setVagasTotal(2);
        request.setVagasDisponiveis(1);
        return request;
    }

    private User usuario(UUID id, User.Tipo tipo) {
        return User.builder().id(id).tipoConta(tipo).build();
    }

    private Imovel imovel(UUID id, UUID proprietarioId) {
        return Imovel.builder()
                .id(id)
                .proprietarioId(proprietarioId)
                .tipo(Imovel.Tipo.APARTAMENTO)
                .cep("58429-140")
                .rua("Rua Aprígio Veloso")
                .numero("100")
                .complemento("Bloco A")
                .bairro("Universitário")
                .cidade("Campina Grande")
                .estado("PB")
                .latitude(-7.21528)
                .longitude(-35.90894)
                .ativo(true)
                .build();
    }

    private Anuncio anuncio(UUID id, UUID imovelId, UUID locadorId) {
        return Anuncio.builder()
                .id(id)
                .imovelId(imovelId)
                .locadorId(locadorId)
                .titulo("Apartamento perto da UFCG")
                .tipoOferta(Anuncio.TipoOferta.IMOVEL_COMPLETO)
                .precoAluguel(new BigDecimal("1200.00"))
                .precoCondominio(new BigDecimal("100.00"))
                .precoIptu(BigDecimal.ZERO)
                .status(Anuncio.Status.ATIVO)
                .descricao("Dois quartos")
                .vagasTotal(2)
                .vagasDisponiveis(2)
                .visualizacoes(0)
                .dataPublicacao(OffsetDateTime.parse("2026-01-15T12:00:00Z"))
                .build();
    }
}
