# Análise Estática — Checkstyle (Entrega 3)

Ferramenta: **Checkstyle 10.21.4**, ruleset **Sun** (`sun_checks.xml`), backend Java, via Maven (`mvn checkstyle:checkstyle`).

**2235 violações** em **153 arquivos**. A lógica de negócio não é afetada — a maioria é documentação (Javadoc), imutabilidade de parâmetros (`final`) e formatação, itens de manutenibilidade a longo prazo.

## Por severidade

| Severidade | Qtde |
|---|---|
| error | 2235 |

## Top 15 regras violadas

| Regra (Checkstyle) | Ocorrências | O que é |
|---|---|---|
| `JavadocVariableCheck` | 628 | falta Javadoc em atributo |
| `FinalParametersCheck` | 393 | parâmetro sem `final` |
| `LineLengthCheck` | 387 | linha > 80 colunas |
| `MissingJavadocMethodCheck` | 207 | método sem Javadoc |
| `DesignForExtensionCheck` | 153 | classe/método não preparado p/ herança |
| `MagicNumberCheck` | 94 | número mágico |
| `JavadocMethodCheck` | 84 | Javadoc de método incompleto |
| `NewlineAtEndOfFileCheck` | 58 | sem newline no fim do arquivo |
| `JavadocPackageCheck` | 56 | sem package-info |
| `WhitespaceAroundCheck` | 52 | espaçamento |
| `HiddenFieldCheck` | 35 | parâmetro sombreia atributo |
| `NeedBracesCheck` | 25 | — |
| `OperatorWrapCheck` | 18 | — |
| `RedundantModifierCheck` | 12 | — |
| `LeftCurlyCheck` | 8 | — |
