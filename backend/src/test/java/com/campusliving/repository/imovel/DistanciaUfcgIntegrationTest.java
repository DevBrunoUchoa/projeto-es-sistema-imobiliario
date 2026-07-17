package com.campusliving.repository.imovel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.campusliving.TestcontainersConfiguration;
import com.campusliving.model.usuario.User;
import com.campusliving.repository.usuario.UserRepository;

/**
 * RF-16: valida o cálculo de distância até a UFCG via PostGIS (a coluna geom é
 * mantida em sincronia com latitude/longitude por trigger).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
class DistanciaUfcgIntegrationTest {

    private static final double UFCG_LAT = -7.21528;
    private static final double UFCG_LON = -35.90894;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AnuncioRepository anuncioRepository;
    @Autowired
    private UserRepository userRepository;

    private UUID inserirImovel(double lat, double lon) {
        User locador = userRepository.saveAndFlush(User.builder()
                .nome("Locador Geo")
                .email("geo-" + UUID.randomUUID() + "@teste.com")
                .senhaHash("hash-fake")
                .tipoConta(User.Tipo.LOCADOR)
                .build());
        UUID imovelId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO properties (id, proprietario_id, tipo, cep, rua, numero, bairro, latitude, longitude)
                VALUES (?, ?, 'QUARTO', '58400-000', 'Rua Teste', '100', 'Centro', ?, ?)
                """, imovelId, locador.getId(), lat, lon);
        return imovelId;
    }

    @Test
    void imovelNoCampus_distanciaProximaDeZero() {
        UUID imovelId = inserirImovel(UFCG_LAT, UFCG_LON);
        Integer metros = anuncioRepository.calcularDistanciaUfcgMetros(imovelId, UFCG_LAT, UFCG_LON);
        assertThat(metros).isNotNull().isLessThan(5);
    }

    @Test
    void imovelAUmQuilometro_distanciaAproximada() {
        // ~1 km ao norte (0.009° de latitude ≈ 1000 m).
        UUID imovelId = inserirImovel(UFCG_LAT + 0.009, UFCG_LON);
        Integer metros = anuncioRepository.calcularDistanciaUfcgMetros(imovelId, UFCG_LAT, UFCG_LON);
        assertThat(metros).isNotNull().isBetween(850, 1150);
    }
}
