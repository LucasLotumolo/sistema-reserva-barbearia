import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import {
  adicionarServico,
  avaliar,
  buscarAgendamento,
  cancelar,
  confirmarPresenca,
  ErroDaApi,
  reagendar,
  removerServico,
  type Agendamento,
} from '../api';
import { nomeDoBarbeiro, SERVICOS } from '../catalogo';
import { SeletorDeHorario } from '../components/SeletorDeHorario';
import { formatarDia, formatarDuracao, formatarHora, formatarPreco } from '../datas';
import {
  aberturaDaConfirmacao,
  descreverStatus,
  DURACAO_MAXIMA_EM_MINUTOS,
  MAXIMO_DE_SERVICOS,
  podeAlterarServicos,
  podeAvaliar,
  podeCancelar,
  podeReagendar,
  reagendamentosRestantes,
  situacaoDaConfirmacao,
} from '../regras';

const NOTAS = [1, 2, 3, 4, 5];

export function DetalheDoHorario() {
  const { id = '' } = useParams();
  const location = useLocation();
  const [agendamento, setAgendamento] = useState<Agendamento | null>(null);
  const [aviso, setAviso] = useState<string | null>((location.state as { aviso?: string } | null)?.aviso ?? null);
  const [erro, setErro] = useState<string | null>(null);
  const [ocupado, setOcupado] = useState(false);
  const [reagendando, setReagendando] = useState(false);
  const [novoInicio, setNovoInicio] = useState<string | null>(null);
  const [cancelando, setCancelando] = useState(false);

  const carregar = useCallback(async () => {
    setAgendamento(await buscarAgendamento(id));
  }, [id]);

  useEffect(() => {
    carregar().catch((falha: ErroDaApi) => setErro(falha.message));
  }, [carregar]);

  async function executar(acao: () => Promise<unknown>, mensagem: string) {
    setOcupado(true);
    setErro(null);
    setAviso(null);
    try {
      await acao();
      await carregar();
      setAviso(mensagem);
    } catch (falha) {
      setErro((falha as ErroDaApi).message);
    } finally {
      setOcupado(false);
    }
  }

  if (!agendamento) {
    return erro ? (
      <section className="vazio">
        <p className="erro" role="alert">{erro}</p>
        <Link to="/" className="botao">Voltar para a agenda</Link>
      </section>
    ) : (
      <p className="nota" role="status">Abrindo o horário…</p>
    );
  }

  const confirmacao = situacaoDaConfirmacao(agendamento);
  const adicionaveis = SERVICOS.filter(
    (servico) => agendamento.duracaoTotalEmMinutos + servico.duracaoEmMinutos <= DURACAO_MAXIMA_EM_MINUTOS,
  );
  const alteravel = podeAlterarServicos(agendamento);

  return (
    <article className="detalhe">
      <Link to="/" className="voltar">Voltar para a agenda</Link>

      <header className="detalhe__cabeca">
        <p className="detalhe__dia">{formatarDia(agendamento.inicio)}</p>
        <h1>{formatarHora(agendamento.inicio)}</h1>
        <p>
          Até {formatarHora(agendamento.fim)} com {nomeDoBarbeiro(agendamento.barbeiroId)}.{' '}
          <span className="linha__status" data-status={agendamento.status}>{descreverStatus(agendamento)}.</span>
        </p>
      </header>

      <div aria-live="polite">
        {aviso && <p className="aviso" role="status">{aviso}</p>}
        {erro && <p className="erro" role="alert">{erro}</p>}
      </div>

      {confirmacao === 'aberta' && (
        <section className="bloco">
          <h2>Você vem?</h2>
          <p>Sem confirmação até 30 minutos antes, o horário é liberado para outro cliente.</p>
          <button
            type="button"
            className="botao"
            disabled={ocupado}
            onClick={() => executar(() => confirmarPresenca(agendamento.id), 'Presença confirmada.')}
          >
            Confirmar presença
          </button>
        </section>
      )}
      {confirmacao === 'aguardando' && (
        <p className="nota">
          A confirmação de presença abre {formatarDia(aberturaDaConfirmacao(agendamento))}, às{' '}
          {formatarHora(aberturaDaConfirmacao(agendamento))}.
        </p>
      )}

      {podeAvaliar(agendamento) && (
        <section className="bloco">
          <h2>Como foi o atendimento?</h2>
          <div className="notas" role="group" aria-label="Nota de 1 a 5">
            {NOTAS.map((nota) => (
              <button
                key={nota}
                type="button"
                className="nota-botao"
                disabled={ocupado}
                aria-label={`Dar nota ${nota}`}
                onClick={() => executar(() => avaliar(agendamento.id, nota), `Avaliação registrada: nota ${nota}.`)}
              >
                {nota}
              </button>
            ))}
          </div>
        </section>
      )}
      {agendamento.avaliacao && (
        <p className="nota">Você deu nota {agendamento.avaliacao.nota} para este atendimento.</p>
      )}

      <section className="bloco">
        <h2>Serviços</h2>
        <ul className="itens">
          {agendamento.itens.map((item) => (
            <li key={item.id}>
              <span>{item.nomeDoServico}</span>
              <span>{formatarDuracao(item.duracaoEmMinutos)}</span>
              <span>{formatarPreco(item.preco)}</span>
              {alteravel && agendamento.itens.length > 1 && (
                <button
                  type="button"
                  className="botao-texto"
                  data-testid={`remover-${item.nomeDoServico}`}
                  disabled={ocupado}
                  onClick={() => executar(() => removerServico(agendamento.id, item.id), `${item.nomeDoServico} removido.`)}
                >
                  Remover
                </button>
              )}
            </li>
          ))}
          <li className="itens__total">
            <span>Total</span>
            <span>{formatarDuracao(agendamento.duracaoTotalEmMinutos)}</span>
            <span>{formatarPreco(agendamento.valorTotal)}</span>
          </li>
        </ul>

        {alteravel && agendamento.itens.length < MAXIMO_DE_SERVICOS && (
          <div className="adicionar">
            <span>Incluir:</span>
            {adicionaveis.map((servico) => (
              <button
                key={servico.id}
                type="button"
                className="ficha"
                data-testid={`incluir-${servico.nome}`}
                disabled={ocupado}
                onClick={() => executar(() => adicionarServico(agendamento.id, servico.id), `${servico.nome} incluído.`)}
              >
                {servico.nome}
              </button>
            ))}
          </div>
        )}
      </section>

      {podeReagendar(agendamento) && (
        <section className="bloco">
          <h2>Mudar o horário</h2>
          {!reagendando ? (
            <>
              <p>
                {reagendamentosRestantes(agendamento) === 1
                  ? 'Você ainda pode mudar este horário 1 vez.'
                  : `Você ainda pode mudar este horário ${reagendamentosRestantes(agendamento)} vezes.`}
              </p>
              <button type="button" className="botao botao--secundario" onClick={() => setReagendando(true)}>
                Escolher outro horário
              </button>
            </>
          ) : (
            <>
              <SeletorDeHorario
                barbeiroId={agendamento.barbeiroId}
                servicos={agendamento.itens.map((item) => item.servicoId)}
                duracaoEmMinutos={agendamento.duracaoTotalEmMinutos}
                antecedenciaEmMinutos={0}
                selecionado={novoInicio}
                aoSelecionar={setNovoInicio}
              />
              <div className="acoes">
                <button
                  type="button"
                  className="botao"
                  disabled={ocupado || !novoInicio}
                  onClick={() =>
                    executar(async () => {
                      await reagendar(agendamento.id, novoInicio!);
                      setReagendando(false);
                      setNovoInicio(null);
                    }, 'Horário alterado.')
                  }
                >
                  Mudar para este horário
                </button>
                <button type="button" className="botao-texto" onClick={() => { setReagendando(false); setNovoInicio(null); }}>
                  Manter o horário atual
                </button>
              </div>
            </>
          )}
        </section>
      )}

      {podeCancelar(agendamento) && (
        <section className="bloco bloco--perigo">
          {!cancelando ? (
            <button type="button" className="botao-texto botao-texto--perigo" onClick={() => setCancelando(true)}>
              Cancelar este horário
            </button>
          ) : (
            <>
              <p>O horário volta a ficar livre para outros clientes, e não é possível desfazer.</p>
              <div className="acoes">
                <button
                  type="button"
                  className="botao botao--perigo"
                  disabled={ocupado}
                  onClick={() =>
                    executar(async () => {
                      await cancelar(agendamento.id);
                      setCancelando(false);
                    }, 'Horário cancelado.')
                  }
                >
                  Cancelar horário
                </button>
                <button type="button" className="botao-texto" onClick={() => setCancelando(false)}>
                  Manter horário
                </button>
              </div>
            </>
          )}
        </section>
      )}
    </article>
  );
}
