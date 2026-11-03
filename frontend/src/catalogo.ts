// A API não expõe listagem de serviços nem de barbeiros. Os serviços espelham o seed de
// src/main/resources/schema.sql; barbeiro não é entidade no back-end, que aceita qualquer UUID.
export interface ServicoDoCatalogo {
  id: string;
  nome: string;
  preco: number;
  duracaoEmMinutos: number;
}

export interface Barbeiro {
  id: string;
  nome: string;
}

export const SERVICOS: ServicoDoCatalogo[] = [
  { id: '11111111-1111-1111-1111-111111111111', nome: 'Corte', preco: 40, duracaoEmMinutos: 30 },
  { id: '22222222-2222-2222-2222-222222222222', nome: 'Barba', preco: 25, duracaoEmMinutos: 20 },
  { id: '33333333-3333-3333-3333-333333333333', nome: 'Sobrancelha', preco: 15, duracaoEmMinutos: 15 },
];

export const BARBEIROS: Barbeiro[] = [
  { id: 'aaaaaaaa-0000-4000-8000-000000000001', nome: 'Almir' },
  { id: 'aaaaaaaa-0000-4000-8000-000000000002', nome: 'Neide' },
  { id: 'aaaaaaaa-0000-4000-8000-000000000003', nome: 'Tavares' },
];

export function nomeDoBarbeiro(id: string): string {
  return BARBEIROS.find((barbeiro) => barbeiro.id === id)?.nome ?? 'Barbeiro não identificado';
}
