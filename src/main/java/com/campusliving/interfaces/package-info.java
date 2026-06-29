/**
 * Camada de <strong>Interfaces</strong> (adaptadores de entrada / entrega).
 *
 * <p>Expõe a aplicação ao mundo externo. Em um backend REST, concentra os
 * controladores HTTP, os DTOs de requisição/resposta, mapeadores e o tratamento
 * global de erros. Traduz chamadas externas em invocações de casos de uso.</p>
 *
 * <p><strong>Regra de dependência:</strong> depende de {@code application}
 * (e, indiretamente, de {@code domain}). Não contém regra de negócio — apenas
 * adaptação de entrada/saída.</p>
 *
 * <p>Subpacotes sugeridos para a evolução do sistema:</p>
 * <ul>
 *   <li>{@code rest} — controladores REST;</li>
 *   <li>{@code rest.dto} — objetos de requisição e resposta;</li>
 *   <li>{@code rest.handler} — tratamento global de exceções (@RestControllerAdvice).</li>
 * </ul>
 */
package com.campusliving.interfaces;
