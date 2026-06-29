package com.campusliving;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação.
 *
 * <p>Fica no pacote raiz {@code com.campusliving} de forma proposital: o
 * {@code @SpringBootApplication} habilita o component scan a partir deste
 * pacote, alcançando todas as camadas da Clean Architecture
 * ({@code domain}, {@code application}, {@code infrastructure},
 * {@code interfaces} e {@code config}).</p>
 */
@SpringBootApplication
public class CampusLivingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusLivingApplication.class, args);
    }
}
