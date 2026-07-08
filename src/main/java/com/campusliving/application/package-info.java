/**
 * Camada de <strong>Aplicação</strong> (casos de uso).
 *
 * <p>Orquestra o fluxo das regras de negócio para atender a um caso de uso
 * específico (ex.: cadastrar usuário, publicar anúncio). Coordena entidades de
 * domínio e ports, mas não contém regras de infraestrutura nem detalhes de
 * entrega (HTTP, JSON).</p>
 *
 * <p><strong>Regra de dependência:</strong> depende apenas de
 * {@code domain}. Não pode depender de {@code infrastructure} nem de
 * {@code interfaces}.</p>
 *
 * <p>Subpacotes sugeridos para a evolução do sistema:</p>
 * <ul>
 *   <li>{@code usecase} — serviços de aplicação / casos de uso;</li>
 *   <li>{@code dto} — comandos e resultados de caso de uso;</li>
 *   <li>{@code port} — interfaces para serviços externos (e-mail, geocodificação).</li>
 * </ul>
 */
package com.campusliving.application;
