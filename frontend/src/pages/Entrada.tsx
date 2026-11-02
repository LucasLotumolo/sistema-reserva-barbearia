import { useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { criarConta, entrar, ErroDaApi } from '../api';
import { useSessao } from '../sessao';

type Modo = 'entrar' | 'criar';

export function Entrada() {
  const { autenticado, aviso, iniciar } = useSessao();
  const [modo, setModo] = useState<Modo>('entrar');
  const [nome, setNome] = useState('');
  const [sobrenome, setSobrenome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  if (autenticado) return <Navigate to="/" replace />;

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);
    try {
      if (modo === 'criar') {
        await criarConta({ name: nome.trim(), lastname: sobrenome.trim(), email: email.trim(), password: senha });
      }
      iniciar(await entrar(email.trim(), senha));
    } catch (falha) {
      setErro((falha as ErroDaApi).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="entrada">
      <div className="entrada__poste" aria-hidden="true">
        <div className="listras listras--abertura" />
      </div>

      <section className="entrada__letreiro">
        <h1>Barbearia Três Cadeiras</h1>
        <p>Escolha os serviços, veja os horários livres de cada barbeiro e marque sem precisar ligar.</p>
      </section>

      <section className="entrada__acesso" aria-label={modo === 'entrar' ? 'Entrar' : 'Criar conta'}>
        <div className="alternador" role="group" aria-label="Tenho conta ou sou novo">
          <button type="button" data-testid="aba-entrar" aria-pressed={modo === 'entrar'} onClick={() => { setModo('entrar'); setErro(null); }}>
            Entrar
          </button>
          <button type="button" data-testid="aba-criar-conta" aria-pressed={modo === 'criar'} onClick={() => { setModo('criar'); setErro(null); }}>
            Criar conta
          </button>
        </div>

        {aviso && !erro && <p className="nota" role="status">{aviso}</p>}

        <form className="formulario" onSubmit={enviar}>
          {modo === 'criar' && (
            <div className="formulario__dupla">
              <label>
                Nome
                <input required autoComplete="given-name" value={nome} onChange={(e) => setNome(e.target.value)} />
              </label>
              <label>
                Sobrenome
                <input required autoComplete="family-name" value={sobrenome} onChange={(e) => setSobrenome(e.target.value)} />
              </label>
            </div>
          )}
          <label>
            E-mail
            <input type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
          </label>
          <label>
            Senha
            <input
              type="password"
              required
              autoComplete={modo === 'entrar' ? 'current-password' : 'new-password'}
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>
          {erro && <p className="erro" role="alert">{erro}</p>}
          <button type="submit" className="botao" data-testid="enviar-acesso" disabled={enviando}>
            {enviando ? 'Aguarde…' : modo === 'entrar' ? 'Entrar' : 'Criar conta'}
          </button>
        </form>
      </section>
    </div>
  );
}
