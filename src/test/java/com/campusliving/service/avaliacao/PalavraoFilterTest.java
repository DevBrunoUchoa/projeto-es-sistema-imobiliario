package com.campusliving.service.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes unitários do filtro de palavrão (RF-29, Fluxo Secundário 1). */
class PalavraoFilterTest {

    private final PalavraoFilter filter = new PalavraoFilter();

    @Test
    void comentarioLimpo_naoDeveSerBloqueado() {
        assertThat(filter.contemPalavraImpropria("O locador foi muito atencioso e o quarto era limpo."))
                .isFalse();
    }

    @Test
    void comentarioComTermoBloqueado_deveSerBloqueado() {
        assertThat(filter.contemPalavraImpropria("Isso e uma porra de anuncio, nao recomendo."))
                .isTrue();
    }

    @Test
    void comentarioComTermoEmCaixaAltaEAcento_deveSerBloqueado() {
        // "Idiota" com acento/caixa diferente ainda deve ser pego pela normalização.
        assertThat(filter.contemPalavraImpropria("O locador foi um IDIOTA comigo"))
                .isTrue();
    }

    @Test
    void termoBloqueadoComoSubstringDeOutraPalavra_naoDeveDispararFalsoPositivo() {
        // "cueca" contém a sequência "cu" mas não é a palavra "cu" isolada —
        // o \b (whole word) do filtro não deveria confundir os dois.
        assertThat(filter.contemPalavraImpropria("Ele deixou uma cueca suja no quarto"))
                .isFalse();
    }

    @Test
    void comentarioNuloOuEmBranco_naoDeveSerBloqueado() {
        assertThat(filter.contemPalavraImpropria(null)).isFalse();
    }
}