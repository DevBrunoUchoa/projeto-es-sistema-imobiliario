export const TIPO_OFERTA_LABELS = {
  IMOVEL_COMPLETO: 'Imóvel completo',
  VAGA_COMPARTILHADA: 'Vaga compartilhada',
};

export const TIPO_IMOVEL_LABELS = {
  APARTAMENTO: 'Apartamento',
  QUARTO: 'Quarto',
  FLAT: 'Flat',
  PENSIONATO: 'Pensionato',
};

export const STATUS_LABELS = {
  ATIVO: 'Ativo',
  INATIVO: 'Inativo',
  ALUGADO: 'Alugado',
};

export function formatMoeda(value) {
  return Number(value ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
