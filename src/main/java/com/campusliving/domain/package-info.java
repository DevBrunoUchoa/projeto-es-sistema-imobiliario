/**
 * Camada de <strong>Domínio</strong> (núcleo da Clean Architecture).
 *
 * <p>Contém as regras de negócio mais estáveis e independentes de tecnologia:
 * entidades/agregados, objetos de valor, exceções de domínio e <em>ports</em>
 * (interfaces de repositório) que descrevem o que o domínio precisa, sem dizer
 * como será implementado.</p>
 *
 * <p><strong>Regra de dependência:</strong> esta camada não depende de nenhuma
 * outra. Não pode referenciar Spring, JPA, web ou qualquer detalhe de
 * infraestrutura.</p>
 *
 * <p>Subpacotes sugeridos para a evolução do sistema:</p>
 * <ul>
 *   <li>{@code model} — entidades e objetos de valor;</li>
 *   <li>{@code repository} — interfaces (ports) de persistência;</li>
 *   <li>{@code exception} — exceções de negócio.</li>
 * </ul>
 */
package com.campusliving.domain;
