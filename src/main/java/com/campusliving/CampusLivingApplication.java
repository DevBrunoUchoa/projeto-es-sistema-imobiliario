package com.campusliving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Ponto de entrada da aplicação.
 *
 * <p>Fica no pacote raiz {@code com.campusliving} de forma proposital: o
 * {@code @SpringBootApplication} habilita o component scan a partir deste
 * pacote, alcançando todas as camadas da Clean Architecture
 * ({@code domain}, {@code application}, {@code infrastructure},
 * {@code interfaces} e {@code config}).</p>
 *
 * <p>{@code @EnableAsync} (T5.5/RF-16): habilita métodos {@code @Async} —
 * usado pelo job de cálculo de distância até a UFCG (ver
 * {@code AnuncioGeoService}), pra não bloquear a resposta de
 * POST /anuncios esperando a query PostGIS. Sem executor customizado por
 * enquanto: usa o {@code SimpleAsyncTaskExecutor} padrão do Spring (thread
 * nova por chamada) — suficiente pro volume esperado neste projeto; trocar
 * por um {@code ThreadPoolTaskExecutor} configurado é uma melhoria futura se
 * o volume de anúncios publicados por segundo crescer.</p>
 */
@SpringBootApplication
@EnableAsync
public class CampusLivingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLivingApplication.class, args);
    }
}
