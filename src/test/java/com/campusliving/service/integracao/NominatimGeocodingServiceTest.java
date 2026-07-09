package com.campusliving.service.integracao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.campusliving.exception.imovel.EnderecoNaoGeocodificavelException;
import com.campusliving.service.integracao.GeocodingService.Coordenadas;

/** Ver comentário em ViaCepValidationServiceTest sobre o uso de MockRestServiceServer. */
class NominatimGeocodingServiceTest {

    private MockRestServiceServer mockServer;
    private NominatimGeocodingService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service = new NominatimGeocodingService(builder, "https://nominatim.openstreetmap.org");
    }

    @Test
    void geocodificar_quandoEnderecoValido_deveRetornarCoordenadas() {
        mockServer.expect(requestTo(containsString("/search")))
                .andRespond(withSuccess("[{\"lat\":\"-7.2157\",\"lon\":\"-35.9099\"}]", MediaType.APPLICATION_JSON));

        Coordenadas coordenadas = service.geocodificar("Rua Sao Paulo, 123, Centro, Campina Grande - PB, Brasil");

        assertThat(coordenadas.latitude()).isEqualTo(-7.2157);
        assertThat(coordenadas.longitude()).isEqualTo(-35.9099);
    }

    @Test
    void geocodificar_quandoRespostaVazia_deveLancarEnderecoNaoGeocodificavel() {
        mockServer.expect(requestTo(containsString("/search")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.geocodificar("Endereco Inexistente, Brasil"))
                .isInstanceOf(EnderecoNaoGeocodificavelException.class);
    }

    @Test
    void geocodificar_quandoServicoIndisponivel_deveLancarEnderecoNaoGeocodificavel() {
        mockServer.expect(requestTo(containsString("/search")))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.geocodificar("Qualquer endereco, Brasil"))
                .isInstanceOf(EnderecoNaoGeocodificavelException.class);
    }
}
