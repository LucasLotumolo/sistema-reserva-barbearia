import { useEffect, useMemo, useState } from 'react';
import { ErroDaApi, horariosDisponiveis, type Periodo } from '../api';
import { formatarHora, iniciosPossiveis, paraDataHora, proximosDias } from '../datas';
import { Poste } from './Poste';

const DIAS_A_MOSTRAR = 14;

interface Props {
  barbeiroId: string;
  servicos: string[];
  duracaoEmMinutos: number;
  antecedenciaEmMinutos: number;
  selecionado: string | null;
  aoSelecionar: (inicio: string | null) => void;
}

export function SeletorDeHorario({
  barbeiroId,
  servicos,
  duracaoEmMinutos,
  antecedenciaEmMinutos,
  selecionado,
  aoSelecionar,
}: Props) {
  const dias = useMemo(() => proximosDias(DIAS_A_MOSTRAR), []);
  const [data, setData] = useState(dias[0].data);
  const [livres, setLivres] = useState<Periodo[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState<string | null>(null);
  const chaveDosServicos = servicos.join(',');

  useEffect(() => {
    let vigente = true;
    setCarregando(true);
    setErro(null);
    horariosDisponiveis(barbeiroId, data, chaveDosServicos.split(','))
      .then((periodos) => {
        if (vigente) setLivres(periodos);
      })
      .catch((falha: ErroDaApi) => {
        if (!vigente) return;
        setLivres([]);
        setErro(falha.message);
      })
      .finally(() => {
        if (vigente) setCarregando(false);
      });
    return () => {
      vigente = false;
    };
  }, [barbeiroId, data, chaveDosServicos]);

  const grupos = useMemo(
    () => iniciosPossiveis(livres, duracaoEmMinutos, antecedenciaEmMinutos),
    [livres, duracaoEmMinutos, antecedenciaEmMinutos],
  );

  useEffect(() => {
    if (carregando || !selecionado) return;
    const aindaExiste = grupos.some((grupo) => grupo.inicios.some((inicio) => paraDataHora(inicio) === selecionado));
    if (!aindaExiste) aoSelecionar(null);
  }, [carregando, grupos, selecionado, aoSelecionar]);

  // A API devolve o intervalo livre a partir de agora; a antecedência mínima empurra o primeiro horário marcável para depois.
  const marcaveis = grupos.map((grupo) => ({ inicio: paraDataHora(grupo.inicios[0]), fim: grupo.periodo.fim }));

  const selecao = selecionado
    ? {
        inicio: selecionado,
        fim: paraDataHora(new Date(new Date(selecionado).getTime() + duracaoEmMinutos * 60_000)),
      }
    : null;

  return (
    <div className="seletor">
      <div className="dias" role="radiogroup" aria-label="Dia do atendimento">
        {dias.map((dia) => (
          <label key={dia.data} className="dia" data-testid={`dia-${dia.data}`}>
            <input
              type="radio"
              name="dia"
              checked={dia.data === data}
              onChange={() => {
                setData(dia.data);
                aoSelecionar(null);
              }}
            />
            <span className="dia__rosto">
              <strong>{dia.titulo}</strong>
              <span>{dia.detalhe}</span>
            </span>
          </label>
        ))}
      </div>

      <div className="seletor__dia">
        <Poste livres={marcaveis} selecao={selecao} girando={carregando} />

        <div className="seletor__horarios" aria-live="polite" aria-busy={carregando}>
          {carregando && <p className="nota">Procurando horários livres…</p>}
          {!carregando && erro && <p className="erro" role="alert">{erro}</p>}
          {!carregando && !erro && grupos.length === 0 && (
            <p className="nota">Não há horário livre neste dia para esses serviços. Escolha outro dia ou outro barbeiro.</p>
          )}
          {!carregando &&
            grupos.map((grupo) => (
              <fieldset key={grupo.periodo.inicio} className="intervalo">
                <legend>Das {formatarHora(grupo.inicios[0])} às {formatarHora(grupo.periodo.fim)}</legend>
                <div className="fichas">
                  {grupo.inicios.map((inicio) => {
                    const valor = paraDataHora(inicio);
                    return (
                      <button
                        key={valor}
                        type="button"
                        className="ficha"
                        data-testid={`horario-${formatarHora(inicio)}`}
                        aria-pressed={valor === selecionado}
                        onClick={() => aoSelecionar(valor)}
                      >
                        {formatarHora(inicio)}
                      </button>
                    );
                  })}
                </div>
              </fieldset>
            ))}
          {!carregando && !erro && grupos.length > 0 && (
            <p className="nota legenda">
              No poste, a parte listrada tem vaga para esses serviços e a parte lisa não tem.
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
