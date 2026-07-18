// Fonte única de verdade dos papéis de acesso usados na navegação e nas rotas.
//
// - PODE_ANUNCIAR: quem é dono de imóvel e publica anúncios.
// - PODE_BUSCAR_MORADIA: quem busca onde morar — favoritar imóveis e procurar
//   roommates. LOCADOR puro fica de fora: não aluga para si mesmo, então essas
//   funcionalidades não fazem sentido para esse papel.

export const PODE_ANUNCIAR = ['LOCADOR', 'MISTO', 'ADMIN'];
export const PODE_BUSCAR_MORADIA = ['ESTUDANTE', 'MISTO', 'ADMIN'];

export const podeAnunciar = (role) => PODE_ANUNCIAR.includes(role);
export const podeBuscarMoradia = (role) => PODE_BUSCAR_MORADIA.includes(role);