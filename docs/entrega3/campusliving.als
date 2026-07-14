/*
 * Especificação Formal — Plataforma de Moradia Universitária (Campus Living / UniStay)
 * Linguagem: Alloy 6 (Alloy Analyzer).
 *
 * Objetivo: transpor as regras de negócio dos Requisitos Funcionais (RF) para
 * lógica formal, verificando matematicamente invariantes de integridade e
 * segurança (unicidade de registros, integridade das relações e proteções de
 * autorização). Cada `fact` referencia o RF que a origina.
 *
 * Como usar no Alloy Analyzer:
 *   - Execute os comandos `run` (Execute) para ver instâncias válidas do modelo.
 *   - Execute os comandos `check` (Check) — nenhuma asserção deve gerar
 *     contraexemplo (o que confirma que os `facts` garantem as invariantes).
 */
module campusliving

// ==================== ENUMERAÇÕES ====================
enum Bool { TRUE, FALSE }

// RNF/SEG-04: papéis do controle de acesso (RBAC)
enum TipoConta { ESTUDANTE, LOCADOR, MISTO, ADMIN }

enum TipoImovel { APARTAMENTO, QUARTO, FLAT, PENSIONATO }
enum TipoOferta { IMOVEL_COMPLETO, VAGA }
enum StatusAnuncio { ANUNCIO_ATIVO, ANUNCIO_INATIVO, ANUNCIO_ALUGADO }
enum StatusMatch { MATCH_PENDENTE, MATCH_ACEITO, MATCH_REJEITADO }
enum StatusVerif { VERIF_PENDENTE, VERIF_APROVADO, VERIF_REJEITADO }
enum StatusDenuncia { DEN_PENDENTE, DEN_RESOLVIDA, DEN_REJEITADA }
enum Nota { N1, N2, N3, N4, N5 } // avaliação de 1 a 5 (RF-29)

// ==================== TIPOS BÁSICOS ====================
sig Email {}
sig Cep {}

// ==================== ENTIDADES ====================
sig Usuario {
    email: one Email,             // RF-01
    tipo: one TipoConta,          // RNF/SEG-04
    emailVerificado: one Bool,    // RF-05
    ativo: one Bool               // exclusão lógica / conta suspensa
}

sig Imovel {
    proprietario: one Usuario,    // RF-11 (FK -> usuarios)
    cep: one Cep,
    tipo: one TipoImovel,
    geocodificado: one Bool       // RF-16 (endereço com lat/lon)
}

sig Anuncio {
    imovel: one Imovel,           // RF-12 (FK -> imoveis)
    locador: one Usuario,
    status: one StatusAnuncio,    // RF-14 (soft delete)
    oferta: one TipoOferta,
    temDistancia: one Bool        // RF-16 (distância pré-computada)
}

sig Interesse {                   // RF-28 (contato/mensagem)
    estudante: one Usuario,
    anuncio: one Anuncio
}

sig Avaliacao {                   // RF-29 / RF-30
    avaliador: one Usuario,
    locadorAvaliado: one Usuario,
    anuncio: one Anuncio,
    nota: one Nota
}

sig PerfilRoommate {              // RF-32
    dono: one Usuario,
    visivel: one Bool
}

sig Match {                       // RF-34
    solicitante: one Usuario,
    destinatario: one Usuario,
    status: one StatusMatch
}

sig Favorito {                    // RF-26
    usuario: one Usuario,
    anuncio: one Anuncio
}

sig Verificacao {                 // RF-08 / RF-09
    locador: one Usuario,
    status: one StatusVerif
}

sig Denuncia {                    // RF-36 / RF-37
    denunciante: one Usuario,
    alvo: one Anuncio,
    status: one StatusDenuncia
}

// ==================== PREDICADOS AUXILIARES ====================
pred podeSerLocador[u: Usuario] { u.tipo in (LOCADOR + MISTO + ADMIN) }

pred mesmoPar[m1, m2: Match] {
    (m1.solicitante = m2.solicitante and m1.destinatario = m2.destinatario)
    or (m1.solicitante = m2.destinatario and m1.destinatario = m2.solicitante)
}

// ==================== INVARIANTES (FACTS) ====================

// RF-01 + RNF: o e-mail é único entre os usuários.
fact EmailUnico { all disj u1, u2: Usuario | u1.email != u2.email }

// RF-11: apenas LOCADOR, MISTO ou ADMIN podem ser donos de imóvel.
fact ImovelPertenceALocador { all i: Imovel | podeSerLocador[i.proprietario] }

// RF-12: o locador do anúncio é o proprietário do imóvel anunciado.
fact AnuncioDoProprietario { all a: Anuncio | a.locador = a.imovel.proprietario }

// RF-12 (fluxo secundário): no máximo um anúncio ATIVO por imóvel.
fact UnicoAnuncioAtivoPorImovel {
    all i: Imovel | lone a: Anuncio | a.imovel = i and a.status = ANUNCIO_ATIVO
}

// RF-16 / RNF/PER-04: anúncio ativo de imóvel geocodificado tem distância pré-computada.
fact DistanciaPreComputada {
    all a: Anuncio |
        (a.status = ANUNCIO_ATIVO and a.imovel.geocodificado = TRUE)
            implies a.temDistancia = TRUE
}

// RF-28: não se registra interesse no próprio anúncio (remetente != proprietário).
fact InteresseNaoNoProprio { all x: Interesse | x.estudante != x.anuncio.locador }

// RF-29: avaliação válida — não avalia a si mesmo, o avaliado é o locador do
// anúncio, e o avaliador precisa ter tido vínculo (interesse) com o anúncio.
fact AvaliacaoValida {
    all v: Avaliacao |
        v.avaliador != v.locadorAvaliado
        and v.locadorAvaliado = v.anuncio.locador
        and (some x: Interesse | x.estudante = v.avaliador and x.anuncio = v.anuncio)
}

// RF-31: no máximo uma avaliação por (avaliador, anúncio).
fact AvaliacaoUnicaPorPar {
    all disj v1, v2: Avaliacao |
        not (v1.avaliador = v2.avaliador and v1.anuncio = v2.anuncio)
}

// RF-34: não se conecta consigo mesmo.
fact MatchNaoReflexivo { all m: Match | m.solicitante != m.destinatario }

// RF-34 (regra): no máximo um match PENDENTE entre um mesmo par (qualquer sentido).
fact UnicoMatchPendentePorPar {
    no disj m1, m2: Match |
        m1.status = MATCH_PENDENTE and m2.status = MATCH_PENDENTE and mesmoPar[m1, m2]
}

// RF-26: favorito é único por (usuário, anúncio) e não se favorita o próprio anúncio.
fact FavoritoUnico {
    all disj f1, f2: Favorito | not (f1.usuario = f2.usuario and f1.anuncio = f2.anuncio)
}
fact FavoritoDeOutros { all f: Favorito | f.usuario != f.anuncio.locador }

// RF-08/09: só se solicita verificação sendo apto a locador; e o selo (aprovação)
// é único por locador.
fact VerificacaoDeLocador { all v: Verificacao | podeSerLocador[v.locador] }
fact UnicaAprovacaoPorLocador {
    all u: Usuario | lone v: Verificacao | v.locador = u and v.status = VERIF_APROVADO
}

// RF-36: não se denuncia o próprio anúncio.
fact DenunciaNaoNoProprio { all d: Denuncia | d.denunciante != d.alvo.locador }

// ==================== ASSERÇÕES (CHECK) ====================
// Nenhuma deve gerar contraexemplo: confirmam que as invariantes se sustentam.

assert SemDuplicidadeDeAnuncioAtivo {
    all i: Imovel | lone a: Anuncio | a.imovel = i and a.status = ANUNCIO_ATIVO
}
check SemDuplicidadeDeAnuncioAtivo for 6

assert SemAutoAvaliacao { all v: Avaliacao | v.avaliador != v.locadorAvaliado }
check SemAutoAvaliacao for 6

assert AvaliadorSempreTeveContato {
    all v: Avaliacao | some x: Interesse | x.estudante = v.avaliador and x.anuncio = v.anuncio
}
check AvaliadorSempreTeveContato for 6

assert SemAutoMatch { all m: Match | m.solicitante != m.destinatario }
check SemAutoMatch for 6

assert AnuncioSempreDeLocadorValido { all a: Anuncio | podeSerLocador[a.locador] }
check AnuncioSempreDeLocadorValido for 6

assert EmailNuncaDuplicado {
    all disj u1, u2: Usuario | u1.email != u2.email
}
check EmailNuncaDuplicado for 6

// ==================== INSTÂNCIAS (RUN) ====================
// Cenário rico: mostra que o modelo é satisfatível com todas as entidades.
pred cenarioCompleto {
    some Anuncio
    some Avaliacao
    some Match
    some Favorito
    some Verificacao
    some Denuncia
    some a: Anuncio | a.status = ANUNCIO_ATIVO
    some u: Usuario | u.tipo = MISTO
}
run cenarioCompleto for 7

// Cenário mínimo de moradia: estudante demonstra interesse e depois avalia.
pred fluxoInteresseAvaliacao {
    some v: Avaliacao | some x: Interesse |
        x.estudante = v.avaliador and x.anuncio = v.anuncio
}
run fluxoInteresseAvaliacao for 5
