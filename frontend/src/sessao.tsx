import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { guardarToken, registrarAoExpirarSessao, tokenAtual } from './api';

interface Sessao {
  autenticado: boolean;
  aviso: string | null;
  iniciar: (token: string) => void;
  encerrar: () => void;
}

const ContextoDeSessao = createContext<Sessao | null>(null);

export function ProvedorDeSessao({ children }: { children: ReactNode }) {
  const [autenticado, setAutenticado] = useState(() => tokenAtual() !== null);
  const [aviso, setAviso] = useState<string | null>(null);

  useEffect(() => {
    registrarAoExpirarSessao(() => {
      guardarToken(null);
      setAviso('Sua sessão expirou. Entre de novo para continuar.');
      setAutenticado(false);
    });
  }, []);

  const valor = useMemo<Sessao>(
    () => ({
      autenticado,
      aviso,
      iniciar: (token) => {
        guardarToken(token);
        setAviso(null);
        setAutenticado(true);
      },
      encerrar: () => {
        guardarToken(null);
        setAviso(null);
        setAutenticado(false);
      },
    }),
    [autenticado, aviso],
  );

  return <ContextoDeSessao.Provider value={valor}>{children}</ContextoDeSessao.Provider>;
}

export function useSessao() {
  const sessao = useContext(ContextoDeSessao);
  if (!sessao) throw new Error('useSessao precisa estar dentro de ProvedorDeSessao');
  return sessao;
}
