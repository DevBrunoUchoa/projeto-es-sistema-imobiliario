package com.campusliving;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provisiona um banco PostgreSQL com a extensão PostGIS para os testes.
 *
 * <p>Usa a mesma imagem do ambiente real ({@code postgis/postgis}), garantindo
 * que os testes rodem contra o mesmo motor de banco usado em produção. O
 * {@code @ServiceConnection} registra automaticamente url/usuário/senha do
 * contêiner, dispensando configuração manual de datasource no profile de teste.</p>
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4")
                    .asCompatibleSubstituteFor("postgres");

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(POSTGIS_IMAGE);
    }
}
