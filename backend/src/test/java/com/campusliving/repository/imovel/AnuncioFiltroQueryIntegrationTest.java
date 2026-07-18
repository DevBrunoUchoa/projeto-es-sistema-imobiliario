package com.campusliving.repository.imovel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.TestcontainersConfiguration;
import com.campusliving.model.imovel.Anuncio;

/**
 * Filtrar anúncios sem texto de busca (query nula) precisa executar contra o
 * PostgreSQL real sem estourar "function lower(bytea) does not exist".
 *
 * <p>Contexto: com {@code :query} nulo dentro de {@code LOWER(CONCAT(...))}, o
 * Hibernate 6 não conseguia inferir o tipo do parâmetro e o bindava como
 * {@code bytea}. O {@code CAST(:query AS String)} na query dá o tipo explícito,
 * forçando o bind como {@code varchar}. A falha se manifestava em produção
 * (via pooler do Supabase, que resolve o tipo do parâmetro no PREPARE); um
 * PostGIS local nem sempre reproduz, então este teste funciona como guarda de
 * fumaça de que a query com filtro e sem texto ao menos executa sem erro.</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AnuncioFiltroQueryIntegrationTest {

    @Autowired
    private AnuncioRepository anuncioRepository;

    @Test
    void filtroSemTextoDeBusca_executaSemErro() {
        assertThatCode(() -> {
            Page<Anuncio> page = anuncioRepository.findByFiltros(
                    Anuncio.Status.ATIVO,
                    null,          // precoMax
                    null,          // distanciaMaxMetros
                    Boolean.TRUE,  // mobiliado (um filtro ativo, sem texto)
                    null,          // permitePets
                    null,          // permiteFumantes
                    null,          // incluiAlimentacao
                    null,          // tipoOferta
                    null,          // query nula
                    PageRequest.of(0, 10));
            assertThat(page).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void buscaComTexto_executaSemErro() {
        assertThatCode(() -> {
            Page<Anuncio> page = anuncioRepository.findByFiltros(
                    Anuncio.Status.ATIVO,
                    null, null, null, null, null, null, null,
                    "casa",
                    PageRequest.of(0, 10));
            assertThat(page).isNotNull();
        }).doesNotThrowAnyException();
    }
}
