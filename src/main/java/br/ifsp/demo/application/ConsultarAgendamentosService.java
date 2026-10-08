package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class ConsultarAgendamentosService {
    private final AgendamentoRepository repository;

    public ConsultarAgendamentosService(AgendamentoRepository repository) {
        this.repository = repository;
    }

    public List<Agendamento> listarDoCliente(ClienteId clienteId) {
        return repository.porCliente(clienteId).stream()
                .sorted(Comparator.comparing(agendamento -> agendamento.getPeriodo().inicio()))
                .toList();
    }

    public Agendamento buscarPorId(AgendamentoId id) {
        return repository.porId(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));
    }

    public List<Periodo> horariosOcupados(BarbeiroId barbeiroId, LocalDate data) {
        return repository.porBarbeiroEData(barbeiroId, data).stream()
                .map(Agendamento::getPeriodo)
                .sorted(Comparator.comparing(Periodo::inicio))
                .toList();
    }

    public List<Agendamento> listarFuturosDoCliente(ClienteId clienteId) {
        return listarDoCliente(clienteId).stream()
                .filter(this::estaAtivo)
                .toList();
    }

    private boolean estaAtivo(Agendamento agendamento) {
        return agendamento.getStatus() == StatusAgendamento.AGENDADO
                || agendamento.getStatus() == StatusAgendamento.CONFIRMADO;
    }
}
