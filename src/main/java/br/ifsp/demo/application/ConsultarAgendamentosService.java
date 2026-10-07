package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.comum.ClienteId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultarAgendamentosService {
    private final AgendamentoRepository repository;

    public ConsultarAgendamentosService(AgendamentoRepository repository) {
        this.repository = repository;
    }

    public List<Agendamento> listarDoCliente(ClienteId clienteId) {
        throw new UnsupportedOperationException("A implementar");
    }
}
