package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;

import java.time.Clock;

public class AvaliarAtendimentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public AvaliarAtendimentoService(AgendamentoRepository agendamentoRepository, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    public void avaliar(AgendamentoId agendamentoId, int nota) {
        throw new UnsupportedOperationException("não implementado");
    }
}
