package com.campusliving.repository.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.TestcontainersConfiguration;
import com.campusliving.model.avaliacao.Avaliacao;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;

/**
 * Testa o trigger criado em V19__add_reputacao_users.sql contra um Postgres
 * real via Testcontainers — nenhum mock consegue verificar isso, já que a
 * lógica de recálculo de reputação vive inteiramente no banco (ver decisão
 * registrada na conversa: trigger SQL em vez de código Java, para garantir
 * consistência sob concorrência).
 *
 * <p>{@code @Transactional} aqui é só para o JUnit fazer ROLLBACK automático
 * ao final de cada teste (mantém o banco limpo entre execuções) — os
 * triggers do Postgres continuam disparando normalmente dentro da transação
 * aberta pelo teste, então isso não interfere no que estamos verificando.</p>
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class AvaliacaoTriggerIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID locadorId;
    private UUID adId;

    // properties/ads são criados via SQL direto (JdbcTemplate) em vez de via
    // JPA/repository: isolamos este teste de qualquer detalhe de
    // implementação dos módulos de imóvel/anúncio (T5.5) — o que importa
    // aqui é só que exista uma linha válida em "ads" para reviews.ad_id
    // referenciar, não como o módulo de imóvel constrói esse anúncio.
    @BeforeEach
    void setUp() {
        User locador = userRepository.save(User.builder()
                .nome("Locador Teste")
                .email("locador-" + UUID.randomUUID() + "@teste.com")
                .senhaHash("hash-fake")
                .tipoConta(User.Tipo.LOCADOR)
                .build());
        locadorId = locador.getId();

        UUID imovelId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO properties (id, proprietario_id, tipo, cep, rua, numero, bairro, latitude, longitude)
                VALUES (?, ?, 'QUARTO', '58400-000', 'Rua Teste', '100', 'Centro', -7.2306, -35.8811)
                """, imovelId, locadorId);

        adId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO ads (id, imovel_id, locador_id, titulo, tipo_oferta, preco_aluguel)
                VALUES (?, ?, ?, 'Quarto de teste', 'VAGA_COMPARTILHADA', 500.00)
                """, adId, imovelId, locadorId);
    }

    @Test
    void aoInserirAvaliacoes_deveAtualizarNotaMediaETotalAvaliacoesDoLocador() {
        UUID avaliador1 = criarEstudante();
        UUID avaliador2 = criarEstudante();

        avaliacaoRepository.save(Avaliacao.builder()
                .avaliadorId(avaliador1).avaliadoId(locadorId).adId(adId)
                .nota((short) 5).comentario("Otimo lugar").contatoPrevio(true).build());

        User locadorAposUmaAvaliacao = userRepository.findById(locadorId).orElseThrow();
        assertThat(locadorAposUmaAvaliacao.getTotalAvaliacoes()).isEqualTo(1);
        assertThat(locadorAposUmaAvaliacao.getNotaMedia()).isEqualByComparingTo(new BigDecimal("5.00"));

        avaliacaoRepository.save(Avaliacao.builder()
                .avaliadorId(avaliador2).avaliadoId(locadorId).adId(adId)
                .nota((short) 3).comentario("Razoavel").contatoPrevio(true).build());

        User locadorAposDuasAvaliacoes = userRepository.findById(locadorId).orElseThrow();
        assertThat(locadorAposDuasAvaliacoes.getTotalAvaliacoes()).isEqualTo(2);
        assertThat(locadorAposDuasAvaliacoes.getNotaMedia()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    void aoExcluirAvaliacao_deveRecalcularReputacaoDoLocador() {
        UUID avaliador = criarEstudante();

        Avaliacao avaliacao = avaliacaoRepository.save(Avaliacao.builder()
                .avaliadorId(avaliador).avaliadoId(locadorId).adId(adId)
                .nota((short) 2).comentario("Fraco").contatoPrevio(true).build());

        // Confirma que a média subiu antes de testar a remoção.
        assertThat(userRepository.findById(locadorId).orElseThrow().getTotalAvaliacoes()).isEqualTo(1);

        avaliacaoRepository.delete(avaliacao);

        User locadorAposExclusao = userRepository.findById(locadorId).orElseThrow();
        assertThat(locadorAposExclusao.getTotalAvaliacoes()).isZero();
        assertThat(locadorAposExclusao.getNotaMedia()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void primeiraAvaliacao_notaMediaDeveAssumirExatamenteAPrimeiraNota() {
        // RF-30, Fluxo Secundário 1: "Primeira avaliação — nota inicial
        // assume exatamente o valor da primeira nota."
        UUID avaliador = criarEstudante();

        assertThat(userRepository.findById(locadorId).orElseThrow().getNotaMedia())
                .isEqualByComparingTo(BigDecimal.ZERO);

        avaliacaoRepository.save(Avaliacao.builder()
                .avaliadorId(avaliador).avaliadoId(locadorId).adId(adId)
                .nota((short) 4).comentario("Bom").contatoPrevio(true).build());

        assertThat(userRepository.findById(locadorId).orElseThrow().getNotaMedia())
                .isEqualByComparingTo(new BigDecimal("4.00"));
    }

    private UUID criarEstudante() {
        return userRepository.save(User.builder()
                .nome("Estudante Teste")
                .email("estudante-" + UUID.randomUUID() + "@teste.com")
                .senhaHash("hash-fake")
                .tipoConta(User.Tipo.ESTUDANTE)
                .build()).getId();
    }
}