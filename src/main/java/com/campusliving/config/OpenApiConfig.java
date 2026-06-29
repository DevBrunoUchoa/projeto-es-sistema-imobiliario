package com.campusliving.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI/Swagger (SpringDoc).
 *
 * <p>Define os metadados gerais da API. Os caminhos públicos do Swagger UI
 * ({@code /swagger-ui}) e do documento OpenAPI ({@code /api-docs}) são
 * definidos em {@code application.yml}, mantendo a configuração de rotas junto
 * das demais propriedades do ambiente.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusLivingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campus Living API")
                        .description("API da plataforma de locação de imóveis para estudantes da UFCG.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Equipe 04 - Engenharia de Software")
                                .url("https://github.com/DevBrunoUchoa/projeto-es-sistema-imobiliario"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
