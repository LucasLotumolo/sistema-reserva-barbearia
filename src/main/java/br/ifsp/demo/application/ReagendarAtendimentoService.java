package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.AgendaDoBarbeiro;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ReagendarAtendimentoService {
    private final AgendamentoRepository repository;
    private final Clock clock;

    public ReagendarAtendimentoService(AgendamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Agendamento reagendar(AgendamentoId id, LocalDateTime novoInicio) {
        Agendamento agendamento = repository.porId(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(agendamento.getBarbeiroId(), novoInicio.toLocalDate(),
                repository.porBarbeiroEData(agendamento.getBarbeiroId(), novoInicio.toLocalDate()));

        agendamento.reagendarPara(novoInicio, agenda, LocalDateTime.now(clock));
        repository.salvar(agendamento);

        return agendamento;
    }
}