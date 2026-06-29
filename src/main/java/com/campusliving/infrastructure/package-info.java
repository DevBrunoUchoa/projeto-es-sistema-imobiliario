/**
 * Camada de <strong>Infraestrutura</strong> (detalhes técnicos / adaptadores de saída).
 *
 * <p>Implementa os ports definidos no domínio/aplicação usando tecnologias
 * concretas: persistência JPA (entidades de tabela, repositórios Spring Data),
 * clientes HTTP, envio de e-mail, geocodificação, etc. É aqui que o framework e
 * o banco de dados aparecem.</p>
 *
 * <p><strong>Regra de dependência:</strong> depende de {@code domain} e
 * {@code application} (implementa suas interfaces). As camadas internas nunca
 * dependem desta.</p>
 *
 * <p>Subpacotes sugeridos para a evolução do sistema:</p>
 * <ul>
 *   <li>{@code persistence} — entidades JPA e adaptadores de repositório;</li>
 *   <li>{@code persistence.entity} / {@code persistence.repository};</li>
 *   <li>{@code external} — integrações externas (geocodificação, e-mail).</li>
 * </ul>
 */
package com.campusliving.infrastructure;
