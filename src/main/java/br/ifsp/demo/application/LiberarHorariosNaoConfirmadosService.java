package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LiberarHorariosNaoConfirmadosService {
    private final AgendamentoRepository repository;
    private final Clock clock;

    public LiberarHorariosNaoConfirmadosService(AgendamentoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public int liberarHorariosNaoConfirmados() {
        LocalDateTime agora = LocalDateTime.now(clock);
        List<Agendamento> agendados = repository.porStatus(StatusAgendamento.AGENDADO);

        int liberados = 0;
        for (Agendamento agendamento : agendados) {
            if (agendamento.liberarSeNaoConfirmado(agora)) {
                repository.salvar(agendamento);
                liberados++;
            }
        }
        return liberados;
    }
}