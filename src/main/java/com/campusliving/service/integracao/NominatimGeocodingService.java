package com.campusliving.service.integracao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.campusliving.exception.imovel.EnderecoNaoGeocodificavelException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Implementação de {@link GeocodingService} via Nominatim (OpenStreetMap,
 * https://nominatim.org) — gratuito, sem chave, mas exige um User-Agent
 * identificável (política de uso do serviço) e no máximo ~1 req/s, o que é
 * mais do que suficiente pro volume de cadastros deste projeto.
 */
@Service
public class NominatimGeocodingService implements GeocodingService {

    private final RestClient restClient;

    public NominatimGeocodingService(RestClient.Builder builder,
                                      @Value("${app.integrations.nominatim.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "CampusLivingBackend/1.0 (projeto academico UFCG)")
                .build();
    }

    @Override
    public Coordenadas geocodificar(String enderecoCompleto) {
        List<NominatimResult> resultados;
        try {
            resultados = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", enderecoCompleto)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<NominatimResult>>() {
                    });
        } catch (RestClientException e) {
            throw new EnderecoNaoGeocodificavelException("servico de geocodificacao indisponivel no momento");
        }

        if (resultados == null || resultados.isEmpty()) {
            throw new EnderecoNaoGeocodificavelException("endereco nao encontrado");
        }

        NominatimResult resultado = resultados.get(0);
        try {
            return new Coordenadas(Double.parseDouble(resultado.lat()), Double.parseDouble(resultado.lon()));
        } catch (NumberFormatException e) {
            throw new EnderecoNaoGeocodificavelException("resposta invalida do servico de geocodificacao");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {
    }
}
