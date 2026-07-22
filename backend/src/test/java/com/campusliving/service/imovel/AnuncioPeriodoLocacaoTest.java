package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.dto.anuncio.AnuncioRequestDTO;
import com.campusliving.dto.anuncio.AnuncioResponseDTO;
import com.campusliving.dto.anuncio.AnuncioUpdateRequestDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.imovel.Anuncio;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.avaliacao.AvaliacaoRepository;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;

/**
 * Cobre a validação de período de locação (janela de disponibilidade e
 * mínimo/máximo de meses) adicionada a publicarAnuncio/editarAnuncio —
 * duração variável, não um contrato fixo.
 */
@ExtendWith(MockitoExtension.class)
class AnuncioPeriodoLocacaoTest {

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
    private AvaliacaoRepository avaliacaoRepository;

    private AnuncioService service;

    private UUID locadorId;
    private UUID imovelId;

    @BeforeEach
    void setUp() {
        service = new AnuncioService(anuncioRepository, imovelRepository, userRepository,
                auditLogService, imagemAnuncioService, avaliacaoRepository);

        locadorId = UUID.randomUUID();
        imovelId = UUID.randomUUID();

        User locador = User.builder().id(locadorId).tipoConta(User.Tipo.LOCADOR).build();
        Imovel imovel = Imovel.builder().id(imovelId).proprietarioId(locadorId).build();

        lenient().when(userRepository.findByEmail("locador@campusliving.com")).thenReturn(List.of(locador));
        lenient().when(imovelRepository.findById(imovelId)).thenReturn(Optional.of(imovel));
        lenient().when(anuncioRepository.findByImovelIdAndStatus(imovelId, Anuncio.Status.ATIVO)).thenReturn(List.of());
        lenient().when(anuncioRepository.calcularDistanciaUfcgMetros(any(), anyDouble(), anyDouble())).thenReturn(null);
        lenient().when(anuncioRepository.save(any(Anuncio.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private AnuncioRequestDTO requestValido() {
        AnuncioRequestDTO request = new AnuncioRequestDTO();
        request.setImovelId(imovelId.toString());
        request.setTitulo("Quarto perto da UFCG");
        request.setTipoOferta("VAGA_COMPARTILHADA");
        request.setPrecoAluguel(new java.math.BigDecimal("800.00"));
        request.setPrecoCondominio(java.math.BigDecimal.ZERO);
        request.setPrecoIptu(java.math.BigDecimal.ZERO);
        request.setVagasTotal(1);
        request.setVagasDisponiveis(1);
        request.setDataDisponivelDe(LocalDate.of(2026, 8, 1));
        return request;
    }

    @Test
    void publicarAnuncio_comJanelaEMesesValidos_persisteOPeriodo() {
        AnuncioRequestDTO request = requestValido();
        request.setDataDisponivelAte(LocalDate.of(2027, 2, 1));
        request.setPeriodoMinMeses(3);
        request.setPeriodoMaxMeses(12);

        AnuncioResponseDTO resultado = service.publicarAnuncio(request, "locador@campusliving.com");

        assertThat(resultado.getDataDisponivelDe()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(resultado.getDataDisponivelAte()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(resultado.getPeriodoMinMeses()).isEqualTo(3);
        assertThat(resultado.getPeriodoMaxMeses()).isEqualTo(12);
    }

    @Test
    void publicarAnuncio_semDataFimNemMinMax_naoRestringe() {
        AnuncioResponseDTO resultado = service.publicarAnuncio(requestValido(), "locador@campusliving.com");

        assertThat(resultado.getDataDisponivelAte()).isNull();
        assertThat(resultado.getPeriodoMinMeses()).isNull();
        assertThat(resultado.getPeriodoMaxMeses()).isNull();
    }

    @Test
    void publicarAnuncio_comDataFimAnteriorADataInicio_rejeita() {
        AnuncioRequestDTO request = requestValido();
        request.setDataDisponivelAte(LocalDate.of(2026, 7, 1)); // antes de dataDisponivelDe

        assertThatThrownBy(() -> service.publicarAnuncio(request, "locador@campusliving.com"))
                .isInstanceOf(ProjectException.class)
                .hasMessageContaining("Data final de disponibilidade");
    }

    @Test
    void publicarAnuncio_comMaximoMenorQueMinimo_rejeita() {
        AnuncioRequestDTO request = requestValido();
        request.setPeriodoMinMeses(12);
        request.setPeriodoMaxMeses(6);

        assertThatThrownBy(() -> service.publicarAnuncio(request, "locador@campusliving.com"))
                .isInstanceOf(ProjectException.class)
                .hasMessageContaining("Máximo de meses");
    }

    @Test
    void editarAnuncio_comMaximoMenorQueMinimo_rejeita() {
        Anuncio anuncioExistente = Anuncio.builder()
                .id(UUID.randomUUID())
                .locadorId(locadorId)
                .status(Anuncio.Status.ATIVO)
                .build();
        when(anuncioRepository.findById(anuncioExistente.getId())).thenReturn(Optional.of(anuncioExistente));

        AnuncioUpdateRequestDTO update = new AnuncioUpdateRequestDTO();
        update.setTitulo("Quarto atualizado");
        update.setTipoOferta("VAGA_COMPARTILHADA");
        update.setPrecoAluguel(new java.math.BigDecimal("900.00"));
        update.setPrecoCondominio(java.math.BigDecimal.ZERO);
        update.setPrecoIptu(java.math.BigDecimal.ZERO);
        update.setVagasTotal(1);
        update.setVagasDisponiveis(1);
        update.setDataDisponivelDe(LocalDate.of(2026, 8, 1));
        update.setPeriodoMinMeses(12);
        update.setPeriodoMaxMeses(3);

        assertThatThrownBy(() -> service.editarAnuncio(anuncioExistente.getId(), update, "locador@campusliving.com"))
                .isInstanceOf(ProjectException.class)
                .hasMessageContaining("Máximo de meses");
    }
}
