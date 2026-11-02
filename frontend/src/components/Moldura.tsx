import type { ReactNode } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { useSessao } from '../sessao';

export function Moldura({ children }: { children: ReactNode }) {
  const { encerrar } = useSessao();

  return (
    <div className="moldura">
      <header className="topo">
        <Link to="/" className="topo__marca">Três Cadeiras</Link>
        <nav className="topo__nav" aria-label="Principal">
          <NavLink to="/" end>Minha agenda</NavLink>
          <NavLink to="/novo">Marcar horário</NavLink>
          <button type="button" className="botao-texto" onClick={encerrar}>Sair</button>
        </nav>
      </header>
      <main className="conteudo">{children}</main>
    </div>
  );
}
