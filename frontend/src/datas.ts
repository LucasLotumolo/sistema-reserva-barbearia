import type { Periodo } from './api';

const MINUTO = 60_000;
const PASSO_EM_MINUTOS = 15;

export const ABERTURA_EM_MINUTOS = 9 * 60;
export const FECHAMENTO_EM_MINUTOS = 19 * 60;

const doisDigitos = (valor: number) => String(valor).padStart(2, '0');

export function paraData(data: Date) {
  return `${data.getFullYear()}-${doisDigitos(data.getMonth() + 1)}-${doisDigitos(data.getDate())}`;
}

// O back-end usa LocalDateTime, sem fuso; toISOString() converteria para UTC e deslocaria o horário.
export function paraDataHora(data: Date) {
  return `${paraData(data)}T${doisDigitos(data.getHours())}:${doisDigitos(data.getMinutes())}:00`;
}

export function minutosDoDia(dataHora: string) {
  const data = new Date(dataHora);
  return data.getHours() * 60 + data.getMinutes();
}

export function formatarHora(dataHora: string | Date) {
  return new Date(dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

export function formatarDia(dataHora: string | Date) {
  return new Date(dataHora).toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
}

export function formatarDuracao(minutos: number) {
  if (minutos < 60) return `${minutos} min`;
  const horas = Math.floor(minutos / 60);
  const resto = minutos % 60;
  return resto === 0 ? `${horas} h` : `${horas} h ${resto} min`;
}

export function formatarPreco(valor: number) {
  return Number(valor).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

export interface DiaDisponivel {
  data: string;
  titulo: string;
  detalhe: string;
}

export function proximosDias(quantidade: number, hoje = new Date()): DiaDisponivel[] {
  return Array.from({ length: quantidade }, (_, indice) => {
    const dia = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() + indice);
    const semana = dia.toLocaleDateString('pt-BR', { weekday: 'short' }).replace('.', '');
    const titulos = ['Hoje', 'Amanhã'];
    return {
      data: paraData(dia),
      titulo: titulos[indice] ?? semana.charAt(0).toUpperCase() + semana.slice(1),
      detalhe: dia.toLocaleDateString('pt-BR', { day: 'numeric', month: 'short' }).replace('.', '').replace(' de ', ' '),
    };
  });
}

export interface GrupoDeHorarios {
  periodo: Periodo;
  inicios: Date[];
}

export function iniciosPossiveis(
  periodos: Periodo[],
  duracaoEmMinutos: number,
  antecedenciaEmMinutos: number,
  agora = new Date(),
): GrupoDeHorarios[] {
  const limite = agora.getTime() + antecedenciaEmMinutos * MINUTO;
  const passo = PASSO_EM_MINUTOS * MINUTO;

  return periodos
    .map((periodo) => {
      const fim = new Date(periodo.fim).getTime();
      const primeiro = Math.max(new Date(periodo.inicio).getTime(), limite);
      const inicios: Date[] = [];
      for (let instante = arredondarParaCima(primeiro, passo); instante + duracaoEmMinutos * MINUTO <= fim; instante += passo) {
        inicios.push(new Date(instante));
      }
      return { periodo, inicios };
    })
    .filter((grupo) => grupo.inicios.length > 0);
}

function arredondarParaCima(instante: number, passo: number) {
  const inicioDoDia = new Date(instante);
  inicioDoDia.setHours(0, 0, 0, 0);
  const desdeAMeiaNoite = instante - inicioDoDia.getTime();
  return inicioDoDia.getTime() + Math.ceil(desdeAMeiaNoite / passo) * passo;
}

export function minutosAte(dataHora: string, agora = new Date()) {
  return (new Date(dataHora).getTime() - agora.getTime()) / MINUTO;
}
