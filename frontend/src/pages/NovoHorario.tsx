import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { agendar, ErroDaApi } from '../api';
import { BARBEIROS, SERVICOS } from '../catalogo';
import { SeletorDeHorario } from '../components/SeletorDeHorario';
import { formatarDia, formatarDuracao, formatarHora, formatarPreco } from '../datas';
import { ANTECEDENCIA_PARA_AGENDAR, DURACAO_MAXIMA_EM_MINUTOS, MAXIMO_DE_SERVICOS } from '../regras';

export function NovoHorario() {
  const navegar = useNavigate();
  const [servicosEscolhidos, setServicosEscolhidos] = useState<string[]>([SERVICOS[0].id]);
  const [barbeiroId, setBarbeiroId] = useState(BARBEIROS[0].id);
  const [inicio, setInicio] = useState<string | null>(null);
  const [nome, setNome] = useState('');
  const [telefone, setTelefone] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const escolhidos = SERVICOS.filter((servico) => servicosEscolhidos.includes(servico.id));
  const duracao = escolhidos.reduce((total, servico) => total + servico.duracaoEmMinutos, 0);
  const valor = escolhidos.reduce((total, servico) => total + servico.preco, 0);
  const excedeLimites = escolhidos.length > MAXIMO_DE_SERVICOS || duracao > DURACAO_MAXIMA_EM_MINUTOS;
  const telefoneSoDigitos = telefone.replace(/\D/g, '');

  function alternarServico(id: string) {
    setInicio(null);
    setServicosEscolhidos((atuais) =>
      atuais.includes(id) ? atuais.filter((atual) => atual !== id) : [...atuais, id],
    );
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!inicio) return setErro('Escolha um horário antes de marcar.');
    if (telefoneSoDigitos.length < 10 || telefoneSoDigitos.length > 11) {
      return setErro('Informe o telefone com DDD, com 10 ou 11 dígitos.');
    }

    setErro(null);
    setEnviando(true);
    try {
      const agendamento = await agendar({
        barbeiroId,
        nomeDoContato: nome.trim(),
        telefoneDoContato: telefoneSoDigitos,
        inicio,
        servicos: servicosEscolhidos,
      });
      navegar(`/horarios/${agendamento.id}`, { state: { aviso: 'Horário marcado.' } });
    } catch (falha) {
      setErro((falha as ErroDaApi).message);
      setEnviando(false);
    }
  }

  return (
    <form className="novo" onSubmit={enviar}>
      <h1>Marcar horário</h1>

      <fieldset className="bloco">
        <legend>O que você vai fazer</legend>
        <div className="opcoes">
          {SERVICOS.map((servico) => (
            <label key={servico.id} className="opcao" data-testid={`servico-${servico.nome}`}>
              <input
                type="checkbox"
                checked={servicosEscolhidos.includes(servico.id)}
                onChange={() => alternarServico(servico.id)}
              />
              <span className="opcao__rosto">
                <strong>{servico.nome}</strong>
                <span>{formatarDuracao(servico.duracaoEmMinutos)}, {formatarPreco(servico.preco)}</span>
              </span>
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset className="bloco">
        <legend>Com quem</legend>
        <div className="opcoes">
          {BARBEIROS.map((barbeiro) => (
            <label key={barbeiro.id} className="opcao" data-testid={`barbeiro-${barbeiro.nome}`}>
              <input
                type="radio"
                name="barbeiro"
                checked={barbeiro.id === barbeiroId}
                onChange={() => { setBarbeiroId(barbeiro.id); setInicio(null); }}
              />
              <span className="opcao__rosto"><strong>{barbeiro.nome}</strong></span>
            </label>
          ))}
        </div>
      </fieldset>

      <fieldset className="bloco">
        <legend>Quando</legend>
        {escolhidos.length === 0 ? (
          <p className="nota">Escolha ao menos um serviço para ver os horários livres.</p>
        ) : (
          <SeletorDeHorario
            barbeiroId={barbeiroId}
            servicos={servicosEscolhidos}
            duracaoEmMinutos={duracao}
            antecedenciaEmMinutos={ANTECEDENCIA_PARA_AGENDAR}
            selecionado={inicio}
            aoSelecionar={setInicio}
          />
        )}
      </fieldset>

      <fieldset className="bloco">
        <legend>Como falar com você</legend>
        <div className="formulario__dupla">
          <label>
            Nome
            <input required autoComplete="name" value={nome} onChange={(e) => setNome(e.target.value)} />
          </label>
          <label>
            Telefone com DDD
            <input
              required
              type="tel"
              inputMode="numeric"
              autoComplete="tel-national"
              placeholder="16999998888"
              value={telefone}
              onChange={(e) => setTelefone(e.target.value)}
            />
          </label>
        </div>
      </fieldset>

      <div className="fecho">
        <p className="fecho__resumo" aria-live="polite">
          {inicio ? (
            <>
              <strong>{formatarDia(inicio)}, às {formatarHora(inicio)}</strong>
              <span>{formatarDuracao(duracao)}, {formatarPreco(valor)}</span>
            </>
          ) : (
            <span>Nenhum horário escolhido.</span>
          )}
        </p>
        {erro && <p className="erro" role="alert">{erro}</p>}
        <button type="submit" className="botao" data-testid="enviar-agendamento" disabled={enviando || !inicio || excedeLimites}>
          {enviando ? 'Marcando…' : 'Marcar horário'}
        </button>
      </div>
    </form>
  );
}
