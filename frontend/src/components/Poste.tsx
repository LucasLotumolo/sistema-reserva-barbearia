import type { Periodo } from '../api';
import { ABERTURA_EM_MINUTOS, FECHAMENTO_EM_MINUTOS, minutosDoDia } from '../datas';

const HORAS_MARCADAS = [9, 11, 13, 15, 17, 19];
const TOTAL = FECHAMENTO_EM_MINUTOS - ABERTURA_EM_MINUTOS;

const posicao = (minutos: number) => ((minutos - ABERTURA_EM_MINUTOS) / TOTAL) * 100;

function faixa(periodo: Periodo) {
  const topo = posicao(minutosDoDia(periodo.inicio));
  return { top: `${topo}%`, height: `${posicao(minutosDoDia(periodo.fim)) - topo}%` };
}

interface Props {
  livres: Periodo[];
  selecao: Periodo | null;
  girando: boolean;
}

// Só visual: os mesmos horários estão na lista de botões ao lado, acessível por teclado e leitor de tela.
export function Poste({ livres, selecao, girando }: Props) {
  return (
    <div className="poste-do-dia" aria-hidden="true">
      <ol className="poste-do-dia__horas">
        {HORAS_MARCADAS.map((hora) => (
          <li key={hora} style={{ top: `${posicao(hora * 60)}%` }}>{hora}h</li>
        ))}
      </ol>
      <div className="poste-do-dia__corpo">
        <div className="poste-do-dia__tubo">
          {girando ? (
            <div className="listras listras--girando" style={{ top: 0, height: '100%' }} />
          ) : (
            livres.map((periodo) => <div key={periodo.inicio} className="listras" style={faixa(periodo)} />)
          )}
          {selecao && !girando && <div className="poste-do-dia__selecao" style={faixa(selecao)} />}
        </div>
      </div>
    </div>
  );
}
