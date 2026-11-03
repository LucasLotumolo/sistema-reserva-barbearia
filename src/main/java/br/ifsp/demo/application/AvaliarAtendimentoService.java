package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AvaliarAtendimentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final Clock clock;

    public AvaliarAtendimentoService(AgendamentoRepository agendamentoRepository, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.clock = clock;
    }

    public void avaliar(AgendamentoId agendamentoId, int nota) {
        Agendamento agendamento = agendamentoRepository.porId(agendamentoId)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));
        agendamento.avaliar(nota, LocalDateTime.now(clock));
        agendamentoRepository.salvar(agendamento);
    }
}
