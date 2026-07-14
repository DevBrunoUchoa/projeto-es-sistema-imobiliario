# Especificação Formal — Alloy (Entrega 3)

Modelo formal do domínio em **Alloy 6**: [`campusliving.als`](campusliving.als).

Transpõe as regras de negócio dos Requisitos Funcionais para lógica de
predicados/teoria dos conjuntos, permitindo **verificar matematicamente** as
invariantes de integridade e autorização com o *Alloy Analyzer*.

## Como executar (Alloy Analyzer)

1. Baixe o Alloy Analyzer 6.x em https://alloytools.org e abra `campusliving.als`.
2. Menu **Execute → Execute All** (ou rode comando a comando).
3. Para cada `run` deve aparecer uma **instância** (modelo satisfatível).
4. Para cada `check` **não deve haver contraexemplo** (mensagem *"No counterexample found"*), o que confirma que os `facts` garantem a invariante. Tire o print dessa mensagem para o relatório.

## Entidades modeladas

`Usuario` (papéis ESTUDANTE/LOCADOR/MISTO/ADMIN), `Imovel`, `Anuncio`,
`Interesse`, `Avaliacao`, `PerfilRoommate`, `Match`, `Favorito`, `Verificacao`,
`Denuncia`.

## Invariantes formalizadas (fact → RF)

| `fact` | Regra formalizada | RF |
|---|---|---|
| `EmailUnico` | e-mail único entre usuários | RF-01 |
| `ImovelPertenceALocador` | só LOCADOR/MISTO/ADMIN possuem imóvel | RF-11 / RNF/SEG-04 |
| `AnuncioDoProprietario` | locador do anúncio = dono do imóvel | RF-12 |
| `UnicoAnuncioAtivoPorImovel` | no máximo 1 anúncio ATIVO por imóvel | RF-12 (fluxo 2) |
| `DistanciaPreComputada` | anúncio ativo geocodificado tem distância | RF-16 / RNF/PER-04 |
| `InteresseNaoNoProprio` | não se registra interesse no próprio anúncio | RF-28 |
| `AvaliacaoValida` | não auto-avalia; avaliado = locador; avaliador teve vínculo | RF-29 |
| `AvaliacaoUnicaPorPar` | 1 avaliação por (avaliador, anúncio) | RF-29/31 |
| `MatchNaoReflexivo` | não se conecta consigo mesmo | RF-34 |
| `UnicoMatchPendentePorPar` | no máximo 1 match PENDENTE por par | RF-34 |
| `FavoritoUnico` / `FavoritoDeOutros` | favorito único; não favorita o próprio | RF-26 |
| `VerificacaoDeLocador` / `UnicaAprovacaoPorLocador` | verificação só de locador; 1 aprovação | RF-08/09 |
| `DenunciaNaoNoProprio` | não se denuncia o próprio anúncio | RF-36 |

## Asserções verificadas (`check`)

`SemDuplicidadeDeAnuncioAtivo`, `SemAutoAvaliacao`, `AvaliadorSempreTeveContato`,
`SemAutoMatch`, `AnuncioSempreDeLocadorValido`, `EmailNuncaDuplicado` — todas
devem terminar **sem contraexemplo**.

> Nota de honestidade: o arquivo `.als` foi escrito para a sintaxe do Alloy 6 e
> revisado manualmente. A execução dos comandos e a captura dos prints do
> *Analyzer* (evidência para o relatório) devem ser feitas pela equipe na
> ferramenta gráfica.
