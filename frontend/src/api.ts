const BASE = import.meta.env.VITE_API_URL ?? '';
const CHAVE_DO_TOKEN = 'tres-cadeiras.token';

export type Status = 'AGENDADO' | 'CONFIRMADO' | 'CANCELADO' | 'EXPIRADO';

export interface Periodo {
  inicio: string;
  fim: string;
}

export interface ItemDeServico {
  id: string;
  servicoId: string;
  nomeDoServico: string;
  preco: number;
  duracaoEmMinutos: number;
}

export interface Agendamento {
  id: string;
  barbeiroId: string;
  nomeDoContato: string;
  telefoneDoContato: string;
  inicio: string;
  fim: string;
  status: Status;
  valorTotal: number;
  duracaoTotalEmMinutos: number;
  quantidadeDeReagendamentos: number;
  itens: ItemDeServico[];
  avaliacao: { nota: number; registradaEm: string } | null;
}

export class ErroDaApi extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

// O navegador pode bloquear o localStorage (janela privada, dados limpos); a sessão só não sobrevive ao recarregar.
let tokenEmMemoria: string | null = null;
try {
  tokenEmMemoria = localStorage.getItem(CHAVE_DO_TOKEN);
} catch {
  tokenEmMemoria = null;
}

let aoExpirarSessao: () => void = () => {};

export function registrarAoExpirarSessao(callback: () => void) {
  aoExpirarSessao = callback;
}

export function tokenAtual() {
  return tokenEmMemoria;
}

export function guardarToken(token: string | null) {
  tokenEmMemoria = token;
  try {
    if (token) localStorage.setItem(CHAVE_DO_TOKEN, token);
    else localStorage.removeItem(CHAVE_DO_TOKEN);
  } catch {
    return;
  }
}

async function requisitar<T>(caminho: string, opcoes: { metodo?: string; corpo?: unknown } = {}): Promise<T> {
  const headers: Record<string, string> = {};
  if (opcoes.corpo !== undefined) headers['Content-Type'] = 'application/json';
  if (tokenEmMemoria) headers.Authorization = `Bearer ${tokenEmMemoria}`;

  let resposta: Response;
  try {
    resposta = await fetch(`${BASE}${caminho}`, {
      method: opcoes.metodo ?? 'GET',
      headers,
      body: opcoes.corpo === undefined ? undefined : JSON.stringify(opcoes.corpo),
    });
  } catch {
    throw new ErroDaApi('O servidor não respondeu. Confira se o back-end está rodando e tente de novo.', 0);
  }

  if (resposta.ok) {
    const texto = await resposta.text();
    return (texto ? JSON.parse(texto) : undefined) as T;
  }

  if ((resposta.status === 401 || resposta.status === 403) && tokenEmMemoria) {
    aoExpirarSessao();
    throw new ErroDaApi('Sua sessão expirou. Entre de novo para continuar.', resposta.status);
  }

  const detalhe = await resposta.json().catch(() => null);
  throw new ErroDaApi(detalhe?.message ?? 'O servidor recusou o pedido. Tente de novo.', resposta.status);
}

export async function entrar(email: string, senha: string): Promise<string> {
  try {
    const { token } = await requisitar<{ token: string }>('/api/v1/authenticate', {
      metodo: 'POST',
      corpo: { username: email, password: senha },
    });
    return token;
  } catch (erro) {
    if (erro instanceof ErroDaApi && erro.status === 401) {
      throw new ErroDaApi('E-mail ou senha incorretos. Confira e tente de novo.', 401);
    }
    throw erro;
  }
}

export async function criarConta(dados: { name: string; lastname: string; email: string; password: string }) {
  try {
    await requisitar('/api/v1/register', { metodo: 'POST', corpo: dados });
  } catch (erro) {
    if (erro instanceof ErroDaApi && erro.status === 409) {
      throw new ErroDaApi('Este e-mail já tem conta. Entre com a sua senha.', 409);
    }
    throw erro;
  }
}

export function listarAgendamentos() {
  return requisitar<Agendamento[]>('/api/v1/agendamentos');
}

export function buscarAgendamento(id: string) {
  return requisitar<Agendamento>(`/api/v1/agendamentos/${id}`);
}

export function horariosDisponiveis(barbeiroId: string, data: string, servicos: string[]) {
  const parametros = new URLSearchParams({ data });
  servicos.forEach((servico) => parametros.append('servicos', servico));
  return requisitar<Periodo[]>(`/api/v1/barbeiros/${barbeiroId}/horarios-disponiveis?${parametros}`);
}

export function agendar(dados: {
  barbeiroId: string;
  nomeDoContato: string;
  telefoneDoContato: string;
  inicio: string;
  servicos: string[];
}) {
  return requisitar<Agendamento>('/api/v1/agendamentos', { metodo: 'POST', corpo: dados });
}

export function reagendar(id: string, novoInicio: string) {
  return requisitar<Agendamento>(`/api/v1/agendamentos/${id}/reagendamento`, {
    metodo: 'POST',
    corpo: { novoInicio },
  });
}

export function confirmarPresenca(id: string) {
  return requisitar<void>(`/api/v1/agendamentos/${id}/confirmacao`, { metodo: 'POST' });
}

export function cancelar(id: string) {
  return requisitar<void>(`/api/v1/agendamentos/${id}/cancelamento`, { metodo: 'POST' });
}

export function avaliar(id: string, nota: number) {
  return requisitar<void>(`/api/v1/agendamentos/${id}/avaliacao`, { metodo: 'POST', corpo: { nota } });
}

export function adicionarServico(id: string, servicoId: string) {
  return requisitar<Agendamento>(`/api/v1/agendamentos/${id}/itens`, { metodo: 'POST', corpo: { servicoId } });
}

export function removerServico(id: string, itemId: string) {
  return requisitar<Agendamento>(`/api/v1/agendamentos/${id}/itens/${itemId}`, { metodo: 'DELETE' });
}
