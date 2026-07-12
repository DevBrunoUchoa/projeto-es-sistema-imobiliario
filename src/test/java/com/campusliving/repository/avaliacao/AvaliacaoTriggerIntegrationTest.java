package com.campusliving.repository.avaliacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.EntityManager;
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
    @Autowired
    private EntityManager entityManager;

    private UUID locadorId;
    private UUID adId;

    @BeforeEach
    void setUp() {
        User locador = userRepository.saveAndFlush(User.builder()
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

        entityManager.flush();
    }

    @Test
    void aoInserirAvaliacoes_deveAtualizarNotaMediaETotalAvaliacoesDoLocador() {
        UUID avaliador1 = criarEstudante();
        UUID avaliador2 = criarEstudante();

        Avaliacao avaliacao1 = Avaliacao.builder()
                .avaliadorId(avaliador1).avaliadoId(locadorId).adId(adId)
                .nota((short) 5).comentario("Otimo lugar").contatoPrevio(true).build();
        avaliacaoRepository.save(avaliacao1);
        entityManager.flush();

        User locadorAposUmaAvaliacao = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locadorAposUmaAvaliacao);
        assertThat(locadorAposUmaAvaliacao.getTotalAvaliacoes()).isEqualTo(1);
        assertThat(locadorAposUmaAvaliacao.getNotaMedia()).isEqualByComparingTo(new BigDecimal("5.00"));

        Avaliacao avaliacao2 = Avaliacao.builder()
                .avaliadorId(avaliador2).avaliadoId(locadorId).adId(adId)
                .nota((short) 3).comentario("Razoavel").contatoPrevio(true).build();
        avaliacaoRepository.save(avaliacao2);
        entityManager.flush();

        User locadorAposDuasAvaliacoes = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locadorAposDuasAvaliacoes);
        assertThat(locadorAposDuasAvaliacoes.getTotalAvaliacoes()).isEqualTo(2);
        assertThat(locadorAposDuasAvaliacoes.getNotaMedia()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    void aoExcluirAvaliacao_deveRecalcularReputacaoDoLocador() {
        UUID avaliador = criarEstudante();

        Avaliacao avaliacao = Avaliacao.builder()
                .avaliadorId(avaliador).avaliadoId(locadorId).adId(adId)
                .nota((short) 2).comentario("Fraco").contatoPrevio(true).build();
        avaliacaoRepository.save(avaliacao);
        entityManager.flush();

        User locador = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locador);
        assertThat(locador.getTotalAvaliacoes()).isEqualTo(1);

        avaliacaoRepository.delete(avaliacao);
        entityManager.flush();

        User locadorAposExclusao = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locadorAposExclusao);
        assertThat(locadorAposExclusao.getTotalAvaliacoes()).isZero();
        assertThat(locadorAposExclusao.getNotaMedia()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void primeiraAvaliacao_notaMediaDeveAssumirExatamenteAPrimeiraNota() {
        UUID avaliador = criarEstudante();

        User locador = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locador);
        assertThat(locador.getNotaMedia()).isEqualByComparingTo(BigDecimal.ZERO);

        Avaliacao avaliacao = Avaliacao.builder()
                .avaliadorId(avaliador).avaliadoId(locadorId).adId(adId)
                .nota((short) 4).comentario("Bom").contatoPrevio(true).build();
        avaliacaoRepository.save(avaliacao);
        entityManager.flush();

        User locadorAtualizado = userRepository.findById(locadorId).orElseThrow();
        entityManager.refresh(locadorAtualizado);
        assertThat(locadorAtualizado.getNotaMedia()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    private UUID criarEstudante() {
        User estudante = userRepository.saveAndFlush(User.builder()
                .nome("Estudante Teste")
                .email("estudante-" + UUID.randomUUID() + "@teste.com")
                .senhaHash("hash-fake")
                .tipoConta(User.Tipo.ESTUDANTE)
                .build());
        return estudante.getId();
    }
}