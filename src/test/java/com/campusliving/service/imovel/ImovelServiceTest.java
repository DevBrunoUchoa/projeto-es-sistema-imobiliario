package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.exception.ProjectException;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import com.campusliving.service.geocoding.GeocodingService;

/** Testes unitários do cadastro de imóveis e da geocodificação do endereço. */
@ExtendWith(MockitoExtension.class)
class ImovelServiceTest {

    @Mock
    private ImovelRepository imovelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private GeocodingService geocodingService;

    private ImovelService service;

    @BeforeEach
    void setUp() {
        service = new ImovelService(imovelRepository, userRepository, auditLogService, geocodingService);
    }

    @Test
    void criarImovel_quandoLocadorInformaCoordenadas_persisteDadosNormalizadosERegistraAuditoria() {
        UUID proprietarioId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        OffsetDateTime dataCriacao = OffsetDateTime.now();
        ImovelRequestDTO request = requestValido();

        when(userRepository.findByEmail("locador@campusliving.com"))
                .thenReturn(List.of(usuario(proprietarioId, User.Tipo.LOCADOR)));
        when(imovelRepository.save(any(Imovel.class))).thenAnswer(invocation -> {
            Imovel imovel = invocation.getArgument(0);
            imovel.setId(imovelId);
            imovel.setDataCriacao(dataCriacao);
            return imovel;
        });

        ImovelResponseDTO resultado = service.criarImovel(request, "locador@campusliving.com");

        ArgumentCaptor<Imovel> imovelCaptor = ArgumentCaptor.forClass(Imovel.class);
        verify(imovelRepository).save(imovelCaptor.capture());
        Imovel salvo = imovelCaptor.getValue();
        assertThat(salvo.getProprietarioId()).isEqualTo(proprietarioId);
        assertThat(salvo.getTipo()).isEqualTo(Imovel.Tipo.APARTAMENTO);
        assertThat(salvo.getCep()).isEqualTo("58400000");
        assertThat(salvo.getRua()).isEqualTo("Rua das Flores");
        assertThat(salvo.getNumero()).isEqualTo("123");
        assertThat(salvo.getComplemento()).isEqualTo("Apto 203");
        assertThat(salvo.getBairro()).isEqualTo("Centro");
        assertThat(salvo.getCidade()).isEqualTo("Campina Grande");
        assertThat(salvo.getEstado()).isEqualTo("PB");
        assertThat(salvo.getLatitude()).isEqualTo(-7.23056);
        assertThat(salvo.getLongitude()).isEqualTo(-35.88111);
        assertThat(salvo.isAtivo()).isTrue();

        assertThat(resultado.getId()).isEqualTo(imovelId);
        assertThat(resultado.getProprietarioId()).isEqualTo(proprietarioId);
        assertThat(resultado.getTipo()).isEqualTo("APARTAMENTO");
        assertThat(resultado.getCep()).isEqualTo("58400000");
        assertThat(resultado.getLatitude()).isEqualTo(-7.23056);
        assertThat(resultado.getLongitude()).isEqualTo(-35.88111);
        assertThat(resultado.getAtivo()).isTrue();
        assertThat(resultado.getDataCriacao()).isEqualTo(dataCriacao);
        assertThat(resultado.getMensagem()).isEqualTo("Imóvel criado com sucesso!");
        verify(auditLogService).registrarAcao(proprietarioId, "CRIAR_IMOVEL", "Imovel", imovelId);
        verifyNoInteractions(geocodingService);
    }

    @Test
    void criarImovel_quandoFaltaUmaCoordenada_geocodificaEnderecoEPermiteAdmin() {
        UUID proprietarioId = UUID.randomUUID();
        UUID imovelId = UUID.randomUUID();
        ImovelRequestDTO request = requestValido();
        request.setLatitude(null);

        when(userRepository.findByEmail("admin@campusliving.com"))
                .thenReturn(List.of(usuario(proprietarioId, User.Tipo.ADMIN)));
        when(geocodingService.geocodificar("Rua das Flores, 123, Centro, Campina Grande, PB, 58400-000"))
                .thenReturn(Optional.of(new GeocodingService.Coordenadas(-7.2189, -35.8754)));
        when(imovelRepository.save(any(Imovel.class))).thenAnswer(invocation -> {
            Imovel imovel = invocation.getArgument(0);
            imovel.setId(imovelId);
            return imovel;
        });

        ImovelResponseDTO resultado = service.criarImovel(request, "admin@campusliving.com");

        ArgumentCaptor<Imovel> imovelCaptor = ArgumentCaptor.forClass(Imovel.class);
        verify(imovelRepository).save(imovelCaptor.capture());
        assertThat(imovelCaptor.getValue().getLatitude()).isEqualTo(-7.2189);
        assertThat(imovelCaptor.getValue().getLongitude()).isEqualTo(-35.8754);
        assertThat(resultado.getLatitude()).isEqualTo(-7.2189);
        assertThat(resultado.getLongitude()).isEqualTo(-35.8754);
        verify(geocodingService)
                .geocodificar("Rua das Flores, 123, Centro, Campina Grande, PB, 58400-000");
        verify(auditLogService).registrarAcao(proprietarioId, "CRIAR_IMOVEL", "Imovel", imovelId);
    }

    @Test
    void criarImovel_quandoEnderecoNaoPodeSerGeocodificado_retornaErro422SemPersistir() {
        UUID proprietarioId = UUID.randomUUID();
        ImovelRequestDTO request = requestValido();
        request.setLatitude(null);
        request.setLongitude(null);
        when(userRepository.findByEmail(anyString()))
                .thenReturn(List.of(usuario(proprietarioId, User.Tipo.LOCADOR)));
        when(geocodingService.geocodificar(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criarImovel(request, "locador@campusliving.com"))
                .isInstanceOfSatisfying(ProjectException.class, exception -> {
                    assertThat(exception.getMessage()).isEqualTo(
                            "Não foi possível localizar o endereço. Informe latitude e longitude.");
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                });

        verify(imovelRepository, never()).save(any(Imovel.class));
        verifyNoInteractions(auditLogService);
    }

    @Test
    void criarImovel_quandoProprietarioNaoExiste_rejeitaAntesDeGeocodificarOuPersistir() {
        when(userRepository.findByEmail("ausente@campusliving.com")).thenReturn(List.of());

        assertThatThrownBy(() -> service.criarImovel(requestValido(), "ausente@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Proprietário não encontrado");

        verifyNoInteractions(geocodingService, imovelRepository, auditLogService);
    }

    @ParameterizedTest
    @EnumSource(value = User.Tipo.class, names = {"ESTUDANTE", "MISTO"})
    void criarImovel_quandoTipoDeContaNaoTemPermissao_rejeitaAntesDeGeocodificarOuPersistir(
            User.Tipo tipoConta) {
        UUID usuarioId = UUID.randomUUID();
        when(userRepository.findByEmail(anyString())).thenReturn(List.of(usuario(usuarioId, tipoConta)));

        assertThatThrownBy(() -> service.criarImovel(requestValido(), "usuario@campusliving.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Apenas LOCADOR ou ADMIN podem criar imóveis");

        verifyNoInteractions(geocodingService, imovelRepository, auditLogService);
    }

    private ImovelRequestDTO requestValido() {
        ImovelRequestDTO request = new ImovelRequestDTO();
        request.setTipo("APARTAMENTO");
        request.setCep("58400-000");
        request.setRua("Rua das Flores");
        request.setNumero("123");
        request.setComplemento("Apto 203");
        request.setBairro("Centro");
        request.setCidade("Campina Grande");
        request.setEstado("PB");
        request.setLatitude(-7.23056);
        request.setLongitude(-35.88111);
        return request;
    }

    private User usuario(UUID id, User.Tipo tipoConta) {
        return User.builder()
                .id(id)
                .email("usuario@campusliving.com")
                .tipoConta(tipoConta)
                .build();
    }
}
