package com.campusliving.service.integracao;

/**
 * RF-11/RF-16: resolve um endereço textual em coordenadas geográficas.
 *
 * <p>Usada de forma síncrona na criação do imóvel (T5.5.1): como
 * {@code properties.latitude}/{@code longitude} são {@code NOT NULL}, o
 * imóvel não pode ser salvo sem coordenadas — não dá pra "geocodificar
 * depois, de forma assíncrona" nesse ponto específico, mesmo a RF-16 falando
 * de um job assíncrono (esse job cuida da distância até a UFCG a partir de
 * coordenadas que já existem, não da geocodificação inicial do endereço —
 * ver comentário em {@code AnuncioGeoService}).</p>
 */
public interface GeocodingService {

    /**
     * @throws com.campusliving.exception.imovel.EnderecoNaoGeocodificavelException
     *         se o serviço externo estiver indisponível ou não encontrar
     *         coordenadas pro endereço informado.
     */
    Coordenadas geocodificar(String enderecoCompleto);

    record Coordenadas(double latitude, double longitude) {
    }
}
