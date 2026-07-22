# Análise Estática — Checkstyle (Entrega 3)

Ferramenta: **Checkstyle 10.21.4**, ruleset **Sun** (`sun_checks.xml`), backend
Java, via Maven (`mvn checkstyle:checkstyle`).

**2.543 violações** em **154 arquivos**. A lógica de negócio não é afetada — a
maioria é documentação (Javadoc), imutabilidade de parâmetros (`final`) e
formatação, itens de manutenibilidade a longo prazo. O ruleset Sun é
propositalmente rigoroso (exige Javadoc em todo atributo/método e linhas ≤ 80
colunas), o que explica o volume mesmo em código bem organizado.

## Por severidade

| Severidade | Qtde |
|---|---|
| error | 2.543 |

## Top 15 regras violadas

| Regra (Checkstyle) | Ocorrências | O que é |
|---|---|---|
| `JavadocVariableCheck` | 705 | falta Javadoc em atributo |
| `LineLengthCheck` | 487 | linha > 80 colunas |
| `FinalParametersCheck` | 448 | parâmetro sem `final` |
| `MissingJavadocMethodCheck` | 230 | método sem Javadoc |
| `DesignForExtensionCheck` | 172 | classe/método não preparado p/ herança |
| `MagicNumberCheck` | 96 | número mágico |
| `JavadocMethodCheck` | 95 | Javadoc de método incompleto |
| `NewlineAtEndOfFileCheck` | 66 | sem newline no fim do arquivo |
| `JavadocPackageCheck` | 56 | sem `package-info` |
| `WhitespaceAroundCheck` | 52 | espaçamento |
| `HiddenFieldCheck` | 36 | parâmetro sombreia atributo |
| `OperatorWrapCheck` | 28 | quebra de linha em operador |
| `NeedBracesCheck` | 25 | bloco sem chaves |
| `RedundantModifierCheck` | 12 | modificador redundante |
| `LeftCurlyCheck` | 8 | posição da chave de abertura |

## Natureza das ocorrências

Agrupando por natureza, cerca de 43% são **documentação** (`Javadoc*`), 18%
**estilo de parâmetro** (`FinalParameters`), 19% **formatação** (`LineLength`,
`Whitespace`, `Newline`, `LeftCurly`, `OperatorWrap`) e o restante são padrões
de **design/manutenção** (`DesignForExtension`, `MagicNumber`, `HiddenField`,
`NeedBraces`, `RedundantModifier`). Nenhuma aponta defeito funcional; são
dívidas de padronização compatíveis com o rigor do ruleset Sun.
