package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class CancelarAgendamentoService {
    private final AgendamentoRepository repository;
    private final Clock clock;

    public CancelarAgendamentoService(AgendamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public void cancelar(AgendamentoId id) {
        Agendamento agendamento = repository.porId(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        agendamento.cancelar(LocalDateTime.now(clock));
        repository.salvar(agendamento);
    }
}