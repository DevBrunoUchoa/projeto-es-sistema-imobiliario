package com.campusliving.service.imovel;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.campusliving.model.imovel.Anuncio;
import com.campusliving.repository.imovel.AnuncioRepository;
import com.campusliving.repository.imovel.ImovelRepository;

/**
 * RF-16: job assíncrono que calcula a distância de um anúncio até a UFCG e
 * estima tempos de deslocamento, chamado a partir de
 * {@code AnuncioServiceImpl} depois de publicar/reativar um anúncio.
 *
 * <p><b>Por que é um bean separado</b>: {@code @Async} do Spring funciona via
 * proxy — chamar um método {@code @Async} de dentro da MESMA classe (self
 * invocation) ignora o proxy e roda de forma síncrona. Por isso este método
 * precisa estar num bean diferente de quem o chama.</p>
 *
 * <p><b>Sobre "geocodificar endereço"</b> (texto da RF-16): a geocodificação
 * do endereço em si já acontece de forma síncrona na criação do imóvel
 * (T5.5.1/{@code ImovelServiceImpl}), porque {@code properties.latitude}/
 * {@code longitude} são {@code NOT NULL} — não dá pra deixar isso pendente
 * até um job rodar depois. O que ESTE job faz de assíncrono, coerente com
 * RNF/PER-04 (não travar a resposta de POST /anuncios numa consulta
 * geoespacial), é usar o ponto já geocodificado do imóvel para calcular a
 * distância até a UFCG via PostGIS.</p>
 *
 * <p><b>Sobre "distância a pé e ônibus"</b>: como decidido, a distância usada
 * é em linha reta (PostGIS {@code ST_Distance} sobre {@code geography}), não
 * uma rota real — não há integração com um serviço de roteamento neste
 * momento. Os tempos a pé/ônibus são estimativas a partir de velocidades
 * médias assumidas (documentadas abaixo), e {@code geoFallback} é sempre
 * marcado {@code true} para deixar isso visível pra quem consumir a API.</p>
 */
@Service
public class AnuncioGeoService {

    // Velocidades médias assumidas para estimar tempo a partir da distância
    // em linha reta — não são medições reais, são um placeholder documentado
    // até uma integração de roteamento real existir.
    private static final double VELOCIDADE_CAMINHADA_M_POR_MIN = 80.0;  // ~4.8 km/h
    private static final double VELOCIDADE_ONIBUS_M_POR_MIN = 300.0;    // ~18 km/h, considerando paradas

    private final AnuncioRepository anuncioRepository;
    private final ImovelRepository imovelRepository;
    private final double ufcgLatitude;
    private final double ufcgLongitude;

    public AnuncioGeoService(
            AnuncioRepository anuncioRepository,
            ImovelRepository imovelRepository,
            @Value("${app.ufcg.latitude}") double ufcgLatitude,
            @Value("${app.ufcg.longitude}") double ufcgLongitude
    ) {
        this.anuncioRepository = anuncioRepository;
        this.imovelRepository = imovelRepository;
        this.ufcgLatitude = ufcgLatitude;
        this.ufcgLongitude = ufcgLongitude;
    }

    @Async
    public void calcularDistanciaUfcg(UUID anuncioId) {
        Anuncio anuncio = anuncioRepository.findById(anuncioId).orElse(null);
        // O anúncio pode ter sido removido/alterado entre o disparo do job e
        // sua execução — nesse caso não há o que atualizar, sem erro.
        if (anuncio == null) {
            return;
        }

        Double distanciaMetros = imovelRepository.calcularDistanciaMetros(
                anuncio.getImovelId(), ufcgLatitude, ufcgLongitude);
        if (distanciaMetros == null) {
            return;
        }

        anuncio.setDistanciaUfcgMetros((int) Math.round(distanciaMetros));
        anuncio.setTempoPeMin((int) Math.round(distanciaMetros / VELOCIDADE_CAMINHADA_M_POR_MIN));
        anuncio.setTempoOnibusMin((int) Math.round(distanciaMetros / VELOCIDADE_ONIBUS_M_POR_MIN));
        anuncio.setGeoFallback(true);

        anuncioRepository.save(anuncio);
    }
}
