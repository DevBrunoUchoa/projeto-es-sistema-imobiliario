package com.campusliving.service.integracao;

/**
 * RF-11: valida que um CEP existe e pertence a Campina Grande-PB, via um
 * serviço externo de CEP. Interface separada da implementação (ver
 * {@link ViaCepValidationService}) pelo mesmo motivo do
 * {@code DocumentStorageService} do T5.4: trocar de provedor no futuro (ou
 * usar um stub em testes) não deve exigir mexer em {@code ImovelServiceImpl}.
 */
public interface CepValidationService {

    /**
     * @throws com.campusliving.exception.imovel.CepInvalidoException
     *         se o CEP estiver mal formado, não existir, ou não for de
     *         Campina Grande-PB.
     */
    void validarCampinaGrande(String cep);
}
