package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.dto.imovel.ImovelRequestDTO;
import com.campusliving.dto.imovel.ImovelResponseDTO;
import com.campusliving.model.imovel.Imovel;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.imovel.ImovelRepository;
import com.campusliving.repository.usuario.UserRepository;
import com.campusliving.service.audit.AuditLogService;
import com.campusliving.service.geocoding.GeocodingService;

/** Cobre as 6 novas comodidades adicionadas ao cadastro de imóvel. */
@ExtendWith(MockitoExtension.class)
class ImovelComodidadesTest {

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
        User locador = User.builder().id(UUID.randomUUID()).tipoConta(User.Tipo.LOCADOR).build();
        when(userRepository.findByEmail("locador@campusliving.com")).thenReturn(List.of(locador));
        when(imovelRepository.save(any(Imovel.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void criarImovel_comNovasComodidadesMarcadas_persisteTodas() {
        ImovelRequestDTO request = new ImovelRequestDTO();
        request.setTipo("QUARTO");
        request.setCep("58400-000");
        request.setRua("Rua Aprígio Veloso");
        request.setNumero("879");
        request.setBairro("Bela Vista");
        request.setCidade("Campina Grande");
        request.setEstado("PB");
        request.setLatitude(-7.2135);
        request.setLongitude(-35.8951);
        request.setSeguranca24h(true);
        request.setLavanderia(true);
        request.setInternetInclusa(true);
        request.setMercadinhoProximo(true);
        request.setGasIncluso(true);
        request.setVagaGaragem(true);

        ImovelResponseDTO resultado = service.criarImovel(request, "locador@campusliving.com");

        assertThat(resultado.isSeguranca24h()).isTrue();
        assertThat(resultado.isLavanderia()).isTrue();
        assertThat(resultado.isInternetInclusa()).isTrue();
        assertThat(resultado.isMercadinhoProximo()).isTrue();
        assertThat(resultado.isGasIncluso()).isTrue();
        assertThat(resultado.isVagaGaragem()).isTrue();
    }

    @Test
    void criarImovel_semMarcarNovasComodidades_ficamFalse() {
        ImovelRequestDTO request = new ImovelRequestDTO();
        request.setTipo("QUARTO");
        request.setCep("58400-000");
        request.setRua("Rua Aprígio Veloso");
        request.setNumero("879");
        request.setBairro("Bela Vista");
        request.setCidade("Campina Grande");
        request.setEstado("PB");
        request.setLatitude(-7.2135);
        request.setLongitude(-35.8951);

        ImovelResponseDTO resultado = service.criarImovel(request, "locador@campusliving.com");

        assertThat(resultado.isSeguranca24h()).isFalse();
        assertThat(resultado.isLavanderia()).isFalse();
        assertThat(resultado.isInternetInclusa()).isFalse();
        assertThat(resultado.isMercadinhoProximo()).isFalse();
        assertThat(resultado.isGasIncluso()).isFalse();
        assertThat(resultado.isVagaGaragem()).isFalse();
    }
}
