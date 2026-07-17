package com.campusliving.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita processamento assíncrono ({@code @Async}) — usado, por exemplo, no
 * envio de e-mails transacionais para não bloquear a requisição HTTP.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
