package com.campusliving;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de fumaça: garante que o contexto da aplicação sobe completamente,
 * incluindo a conexão com o banco PostGIS provisionado pelo Testcontainers.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class CampusLivingApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto não subir, o teste falha automaticamente.
    }
}
