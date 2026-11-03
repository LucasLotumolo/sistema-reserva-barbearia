import { StrictMode, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import '@fontsource/shrikhand';
import '@fontsource-variable/schibsted-grotesk';
import './styles.css';
import { ProvedorDeSessao, useSessao } from './sessao';
import { Moldura } from './components/Moldura';
import { Entrada } from './pages/Entrada';
import { MinhaAgenda } from './pages/MinhaAgenda';
import { NovoHorario } from './pages/NovoHorario';
import { DetalheDoHorario } from './pages/DetalheDoHorario';

function Protegida({ children }: { children: ReactNode }) {
  const { autenticado } = useSessao();
  if (!autenticado) return <Navigate to="/entrar" replace />;
  return <Moldura>{children}</Moldura>;
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ProvedorDeSessao>
        <Routes>
          <Route path="/entrar" element={<Entrada />} />
          <Route path="/" element={<Protegida><MinhaAgenda /></Protegida>} />
          <Route path="/novo" element={<Protegida><NovoHorario /></Protegida>} />
          <Route path="/horarios/:id" element={<Protegida><DetalheDoHorario /></Protegida>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </ProvedorDeSessao>
    </BrowserRouter>
  </StrictMode>,
);
