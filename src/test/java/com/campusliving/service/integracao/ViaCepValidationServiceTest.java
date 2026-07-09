package com.campusliving.service.integracao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.campusliving.exception.imovel.CepInvalidoException;

/**
 * Testa {@link ViaCepValidationService} de ponta a ponta contra um servidor
 * HTTP falso ({@code MockRestServiceServer}, do próprio spring-boot-starter-test)
 * em vez de mockar o {@code RestClient} internamente — a cadeia fluente do
 * RestClient (get().uri().retrieve().body()) é frágil demais de mockar com
 * Mockito puro; o MockRestServiceServer intercepta a requisição HTTP real
 * (sem rede), o que testa o comportamento de verdade.
 */
class ViaCepValidationServiceTest {

    private MockRestServiceServer mockServer;
    private ViaCepValidationService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        service = new ViaCepValidationService(builder, "https://viacep.com.br/ws");
    }

    @Test
    void validarCampinaGrande_quandoCepDeCampinaGrande_naoDeveLancarExcecao() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/58400000/json/"))
                .andRespond(withSuccess("{\"localidade\":\"Campina Grande\",\"uf\":\"PB\",\"erro\":null}",
                        MediaType.APPLICATION_JSON));

        assertThatCode(() -> service.validarCampinaGrande("58400-000")).doesNotThrowAnyException();
    }

    @Test
    void validarCampinaGrande_quandoCepDeOutraCidade_deveLancarCepInvalido() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/01310100/json/"))
                .andRespond(withSuccess("{\"localidade\":\"Sao Paulo\",\"uf\":\"SP\",\"erro\":null}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.validarCampinaGrande("01310-100"))
                .isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void validarCampinaGrande_quandoCepInexistente_deveLancarCepInvalido() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("{\"erro\":true}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> service.validarCampinaGrande("99999-999"))
                .isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void validarCampinaGrande_quandoFormatoInvalido_deveLancarSemChamarApi() {
        // Nenhum mockServer.expect(...) configurado: se o código tentasse
        // fazer uma chamada HTTP mesmo assim, o MockRestServiceServer lançaria
        // um erro de "unexpected request" — o teste passar confirma que a
        // validação de formato é feita antes de qualquer chamada de rede.
        assertThatThrownBy(() -> service.validarCampinaGrande("abc"))
                .isInstanceOf(CepInvalidoException.class);
    }

    @Test
    void validarCampinaGrande_quandoServicoIndisponivel_deveLancarCepInvalido() {
        mockServer.expect(requestTo("https://viacep.com.br/ws/58400000/json/"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.validarCampinaGrande("58400-000"))
                .isInstanceOf(CepInvalidoException.class);
    }
}
