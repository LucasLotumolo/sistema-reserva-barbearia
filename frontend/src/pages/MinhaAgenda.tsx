import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ErroDaApi, listarAgendamentos, type Agendamento } from '../api';
import { nomeDoBarbeiro } from '../catalogo';
import { formatarDia, formatarDuracao, formatarHora, formatarPreco } from '../datas';
import { descreverStatus, estaAtivo, jaTerminou } from '../regras';

const nomesDosServicos = (a: Agendamento) => a.itens.map((item) => item.nomeDoServico).join(', ');

function Linha({ agendamento }: { agendamento: Agendamento }) {
  return (
    <li>
      <Link to={`/horarios/${agendamento.id}`} className="linha">
        <span className="linha__quando">
          <strong>{formatarHora(agendamento.inicio)}</strong>
          <span>{formatarDia(agendamento.inicio)}</span>
        </span>
        <span className="linha__oque">
          {nomesDosServicos(agendamento)} com {nomeDoBarbeiro(agendamento.barbeiroId)}
        </span>
        <span className="linha__status" data-status={agendamento.status}>
          {descreverStatus(agendamento)}
          {agendamento.avaliacao && `, nota ${agendamento.avaliacao.nota}`}
        </span>
      </Link>
    </li>
  );
}

export function MinhaAgenda() {
  const [agendamentos, setAgendamentos] = useState<Agendamento[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    listarAgendamentos()
      .then(setAgendamentos)
      .catch((falha: ErroDaApi) => setErro(falha.message));
  }, []);

  if (erro) return <p className="erro" role="alert">{erro}</p>;
  if (!agendamentos) return <p className="nota" role="status">Abrindo sua agenda…</p>;

  const futuros = agendamentos.filter((a) => estaAtivo(a) && !jaTerminou(a));
  const anteriores = agendamentos.filter((a) => !futuros.includes(a)).reverse();
  const [proximo, ...seguintes] = futuros;

  if (agendamentos.length === 0) {
    return (
      <section className="vazio">
        <h1>Sua agenda está livre</h1>
        <p>Quando você marcar um horário, ele aparece aqui.</p>
        <Link to="/novo" className="botao">Marcar horário</Link>
      </section>
    );
  }

  return (
    <div className="agenda">
      {proximo ? (
        <Link to={`/horarios/${proximo.id}`} className="placa">
          <span className="placa__dia">Seu próximo horário é {formatarDia(proximo.inicio)}</span>
          <strong className="placa__hora">{formatarHora(proximo.inicio)}</strong>
          <span className="placa__detalhe">
            {nomesDosServicos(proximo)} com {nomeDoBarbeiro(proximo.barbeiroId)}, {formatarDuracao(proximo.duracaoTotalEmMinutos)},{' '}
            {formatarPreco(proximo.valorTotal)}
          </span>
          <span className="placa__status">{descreverStatus(proximo)}</span>
        </Link>
      ) : (
        <section className="vazio vazio--curto">
          <h1>Nenhum horário pela frente</h1>
          <Link to="/novo" className="botao">Marcar horário</Link>
        </section>
      )}

      {seguintes.length > 0 && (
        <section>
          <h2>Depois desse</h2>
          <ul className="linhas">{seguintes.map((a) => <Linha key={a.id} agendamento={a} />)}</ul>
        </section>
      )}

      {anteriores.length > 0 && (
        <section>
          <h2>Histórico</h2>
          <ul className="linhas">{anteriores.map((a) => <Linha key={a.id} agendamento={a} />)}</ul>
        </section>
      )}
    </div>
  );
}
