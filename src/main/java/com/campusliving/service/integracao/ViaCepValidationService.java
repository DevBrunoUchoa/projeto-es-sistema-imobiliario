package com.campusliving.service.integracao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.campusliving.exception.imovel.CepInvalidoException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Implementação de {@link CepValidationService} via a API pública ViaCEP
 * (https://viacep.com.br) — gratuita, sem chave de autenticação.
 *
 * <p>Usa {@code RestClient} (Spring 6 / Boot 3, já disponível via
 * spring-boot-starter-web — não precisou adicionar WebFlux/WebClient só por
 * causa desta chamada síncrona e pontual).</p>
 */
@Service
public class ViaCepValidationService implements CepValidationService {

    // CEPs de Campina Grande-PB começam com 581 (faixa 58100-000 a
    // 58109-999) — não usado pra validar sozinho (a API já confirma
    // localidade/UF com mais precisão), só documentado aqui como contexto.
    private static final String CIDADE_ESPERADA = "Campina Grande";
    private static final String UF_ESPERADA = "PB";

    private final RestClient restClient;

    public ViaCepValidationService(RestClient.Builder builder,
                                    @Value("${app.integrations.viacep.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public void validarCampinaGrande(String cepBruto) {
        String cep = normalizar(cepBruto);
        if (cep == null) {
            throw new CepInvalidoException("formato invalido (esperado 8 digitos, com ou sem hifen)");
        }

        ViaCepResponse resposta;
        try {
            resposta = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);
        } catch (RestClientException e) {
            throw new CepInvalidoException("nao foi possivel validar o CEP no momento (servico externo indisponivel)");
        }

        if (resposta == null || Boolean.TRUE.equals(resposta.erro())) {
            throw new CepInvalidoException("CEP nao encontrado");
        }
        if (!CIDADE_ESPERADA.equalsIgnoreCase(resposta.localidade())
                || !UF_ESPERADA.equalsIgnoreCase(resposta.uf())) {
            throw new CepInvalidoException("o imovel deve estar em Campina Grande-PB");
        }
    }

    private String normalizar(String cepBruto) {
        if (cepBruto == null) {
            return null;
        }
        String digitos = cepBruto.replaceAll("\\D", "");
        return digitos.length() == 8 ? digitos : null;
    }

    /** Só os campos que a validação usa — o resto da resposta é ignorado. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ViaCepResponse(String localidade, String uf, Boolean erro) {
    }
}
