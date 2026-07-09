package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
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

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.exception.imovel.CepInvalidoException;
import com.campusliving.exception.imovel.TipoContaNaoPermiteImovelException;
import com.campusliving.exception.usuario.AcessoNegadoException;
import com.campusliving.exception.usuario.UserNotFoundException;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.integracao.CepValidationService;
import com.campusliving.service.integracao.GeocodingService;
import com.campusliving.service.integracao.GeocodingService.Coordenadas;

@ExtendWith(MockitoExtension.class)
class ImovelServiceImplTest {

    @Mock
    private ImovelRepository imovelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CepValidationService cepValidationService;
    @Mock
    private GeocodingService geocodingService;

    private ImovelServiceImpl service;

    private UUID locadorId;
    private User locador;

    @BeforeEach
    void setUp() {
        service = new ImovelServiceImpl(imovelRepository, userRepository, cepValidationService, geocodingService);
        locadorId = UUID.randomUUID();
        locador = User.builder().id(locadorId).nome("Locador").tipoConta(User.Tipo.LOCADOR).build();
    }

    private ImovelRequestDTO dtoBase() {
        return ImovelRequestDTO.builder()
                .tipo(Imovel.Tipo.APARTAMENTO)
                .cep("58400-000")
                .rua("Rua Sao Paulo")
                .numero("123")
                .bairro("Centro")
                .build();
    }

    @Test
    void criar_quandoValido_deveGeocodificarEcadastrar() {
        when(userRepository.findById(locadorId)).thenReturn(Optional.of(locador));
        when(geocodingService.geocodificar(anyString())).thenReturn(new Coordenadas(-7.2157, -35.9099));

        ImovelResponseDTO resultado = service.criar(dtoBase(), locadorId);

        assertThat(resultado.getProprietarioId()).isEqualTo(locadorId);
        assertThat(resultado.getCidade()).isEqualTo("Campina Grande");
        assertThat(resultado.getEstado()).isEqualTo("PB");
        assertThat(resultado.getLatitude()).isEqualTo(-7.2157);
        assertThat(resultado.getLongitude()).isEqualTo(-35.9099);
        assertThat(resultado.isAtivo()).isTrue();
        verify(cepValidationService).validarCampinaGrande("58400-000");
        verify(imovelRepository).save(any(Imovel.class));
    }

    @Test
    void criar_quandoTipoContaEstudante_deveLancarTipoContaNaoPermiteImovelException() {
        User estudante = User.builder().id(locadorId).nome("Estudante").tipoConta(User.Tipo.ESTUDANTE).build();
        when(userRepository.findById(locadorId)).thenReturn(Optional.of(estudante));

        assertThatThrownBy(() -> service.criar(dtoBase(), locadorId))
                .isInstanceOf(TipoContaNaoPermiteImovelException.class);
        verify(cepValidationService, never()).validarCampinaGrande(anyString());
        verify(geocodingService, never()).geocodificar(anyString());
    }

    @Test
    void criar_quandoRequesterNulo_deveLancarAcessoNegado() {
        assertThatThrownBy(() -> service.criar(dtoBase(), null))
                .isInstanceOf(AcessoNegadoException.class);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void criar_quandoUsuarioNaoExiste_deveLancarUserNotFound() {
        when(userRepository.findById(locadorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(dtoBase(), locadorId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void criar_quandoCepInvalido_devePropagarExcecaoSemGeocodificar() {
        when(userRepository.findById(locadorId)).thenReturn(Optional.of(locador));
        doThrow(new CepInvalidoException("CEP nao encontrado")).when(cepValidationService).validarCampinaGrande(anyString());

        assertThatThrownBy(() -> service.criar(dtoBase(), locadorId))
                .isInstanceOf(CepInvalidoException.class);
        verify(geocodingService, never()).geocodificar(anyString());
        verify(imovelRepository, never()).save(any());
    }

    @Test
    void criar_comCidadeEEstadoInformados_deveRespeitarValoresEnviados() {
        when(userRepository.findById(locadorId)).thenReturn(Optional.of(locador));
        when(geocodingService.geocodificar(anyString())).thenReturn(new Coordenadas(-7.0, -35.0));

        ImovelRequestDTO dto = dtoBase();
        dto.setCidade("Campina Grande");
        dto.setEstado("PB");

        ImovelResponseDTO resultado = service.criar(dto, locadorId);

        assertThat(resultado.getCidade()).isEqualTo("Campina Grande");
        assertThat(resultado.getEstado()).isEqualTo("PB");
    }
}
