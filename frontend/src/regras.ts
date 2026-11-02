import type { Agendamento } from './api';
import { minutosAte } from './datas';

// Espelha as regras de Agendamento.java só para decidir o que mostrar; quem valida é a API.
const MAXIMO_DE_REAGENDAMENTOS = 3;
const ANTECEDENCIA_PARA_REAGENDAR = 120;
const JANELA_DE_CONFIRMACAO_ABRE = 24 * 60;
const JANELA_DE_CONFIRMACAO_FECHA = 30;
export const MAXIMO_DE_SERVICOS = 5;
export const DURACAO_MAXIMA_EM_MINUTOS = 240;
export const ANTECEDENCIA_PARA_AGENDAR = 60;

export const estaAtivo = (a: Agendamento) => a.status === 'AGENDADO' || a.status === 'CONFIRMADO';

export const jaTerminou = (a: Agendamento, agora = new Date()) => minutosAte(a.fim, agora) <= 0;

export const podeCancelar = (a: Agendamento, agora = new Date()) => estaAtivo(a) && minutosAte(a.inicio, agora) > 0;

export const reagendamentosRestantes = (a: Agendamento) =>
  Math.max(MAXIMO_DE_REAGENDAMENTOS - a.quantidadeDeReagendamentos, 0);

export const podeReagendar = (a: Agendamento, agora = new Date()) =>
  estaAtivo(a) && reagendamentosRestantes(a) > 0 && minutosAte(a.inicio, agora) >= ANTECEDENCIA_PARA_REAGENDAR;

export const podeAlterarServicos = (a: Agendamento, agora = new Date()) =>
  estaAtivo(a) && minutosAte(a.inicio, agora) > 0;

export const podeAvaliar = (a: Agendamento, agora = new Date()) =>
  a.status === 'CONFIRMADO' && jaTerminou(a, agora) && a.avaliacao === null;

export function situacaoDaConfirmacao(
  a: Agendamento,
  agora = new Date(),
): 'indisponivel' | 'aguardando' | 'aberta' | 'encerrada' {
  if (a.status !== 'AGENDADO') return 'indisponivel';
  const minutos = minutosAte(a.inicio, agora);
  if (minutos > JANELA_DE_CONFIRMACAO_ABRE) return 'aguardando';
  if (minutos < JANELA_DE_CONFIRMACAO_FECHA) return 'encerrada';
  return 'aberta';
}

export function aberturaDaConfirmacao(a: Agendamento) {
  return new Date(new Date(a.inicio).getTime() - JANELA_DE_CONFIRMACAO_ABRE * 60_000);
}

export function descreverStatus(a: Agendamento, agora = new Date()) {
  if (a.status === 'CANCELADO') return 'Cancelado';
  if (a.status === 'EXPIRADO') return 'Expirado por falta de confirmação';
  if (a.status === 'AGENDADO') return 'Falta confirmar presença';
  return jaTerminou(a, agora) ? 'Atendido' : 'Presença confirmada';
}
