package com.campusliving.service.geocoding;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Geocodificação via Nominatim (OpenStreetMap) — gratuito e open-source
 * (RNF/PROC-02/ECO-01). Falhas (rede, endereço não encontrado, timeout) não
 * propagam exceção: retornam {@link Optional#empty()}, e a decisão de como
 * tratar fica com o chamador (RF-16, fluxo secundário).
 *
 * <p>A política de uso do Nominatim exige um {@code User-Agent} identificável e
 * limita a ~1 req/s — por isso o resultado deve ser cacheado/reaproveitado pelo
 * chamador quando possível.</p>
 */
@Service
public class NominatimGeocodingService implements GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodingService.class);

    private final RestClient restClient;
    private final boolean enabled;

    public NominatimGeocodingService(
            @Value("${app.geocoding.enabled:true}") boolean enabled,
            @Value("${app.geocoding.nominatim-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${app.geocoding.user-agent:CampusLiving/1.0 (contato@campusliving.app)}") String userAgent) {
        this.enabled = enabled;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    @Override
    public Optional<Coordenadas> geocodificar(String enderecoCompleto) {
        if (!enabled || enderecoCompleto == null || enderecoCompleto.isBlank()) {
            return Optional.empty();
        }
        try {
            NominatimResult[] resultados = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("format", "json")
                            .queryParam("limit", "1")
                            .queryParam("q", enderecoCompleto)
                            .build())
                    .retrieve()
                    .body(NominatimResult[].class);

            if (resultados == null || resultados.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new Coordenadas(
                    Double.parseDouble(resultados[0].lat()),
                    Double.parseDouble(resultados[0].lon())));
        } catch (Exception e) {
            log.warn("Falha ao geocodificar \"{}\": {}", enderecoCompleto, e.getMessage());
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {}
}
