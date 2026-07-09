package com.campusliving.service.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;

@ExtendWith(MockitoExtension.class)
class AnuncioGeoServiceTest {

    @Mock
    private AnuncioRepository anuncioRepository;
    @Mock
    private ImovelRepository imovelRepository;

    private AnuncioGeoService service;

    private UUID anuncioId;
    private UUID imovelId;

    @BeforeEach
    void setUp() {
        service = new AnuncioGeoService(anuncioRepository, imovelRepository, -7.2157, -35.9099);
        anuncioId = UUID.randomUUID();
        imovelId = UUID.randomUUID();
    }

    @Test
    void calcularDistanciaUfcg_quandoAnuncioExiste_deveCalcularESalvar() {
        Anuncio anuncio = Anuncio.builder().id(anuncioId).imovelId(imovelId).build();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imovelRepository.calcularDistanciaMetros(any(UUID.class), anyDouble(), anyDouble())).thenReturn(2400.0);

        service.calcularDistanciaUfcg(anuncioId);

        ArgumentCaptor<Anuncio> captor = ArgumentCaptor.forClass(Anuncio.class);
        verify(anuncioRepository).save(captor.capture());
        Anuncio salvo = captor.getValue();
        assertThat(salvo.getDistanciaUfcgMetros()).isEqualTo(2400);
        assertThat(salvo.isGeoFallback()).isTrue();
        assertThat(salvo.getTempoPeMin()).isEqualTo((int) Math.round(2400.0 / 80.0));
        assertThat(salvo.getTempoOnibusMin()).isEqualTo((int) Math.round(2400.0 / 300.0));
    }

    @Test
    void calcularDistanciaUfcg_quandoAnuncioNaoExisteMais_naoDeveFazerNada() {
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.empty());

        service.calcularDistanciaUfcg(anuncioId);

        verify(imovelRepository, never()).calcularDistanciaMetros(any(), anyDouble(), anyDouble());
        verify(anuncioRepository, never()).save(any());
    }

    @Test
    void calcularDistanciaUfcg_quandoDistanciaNula_naoDeveAtualizar() {
        Anuncio anuncio = Anuncio.builder().id(anuncioId).imovelId(imovelId).build();
        when(anuncioRepository.findById(anuncioId)).thenReturn(Optional.of(anuncio));
        when(imovelRepository.calcularDistanciaMetros(any(UUID.class), anyDouble(), anyDouble())).thenReturn(null);

        service.calcularDistanciaUfcg(anuncioId);

        verify(anuncioRepository, never()).save(any());
    }
}
