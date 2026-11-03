package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class AgendamentoRepositoryEmMemoria implements AgendamentoRepository {
    private final Map<AgendamentoId, Agendamento> agendamentos = new LinkedHashMap<>();
    private int quantidadeDeSalvamentos;

    AgendamentoRepositoryEmMemoria(Agendamento... iniciais) {
        for (Agendamento agendamento : iniciais) {
            agendamentos.put(agendamento.getId(), agendamento);
        }
    }

    @Override
    public void salvar(Agendamento agendamento) {
        agendamentos.put(agendamento.getId(), agendamento);
        quantidadeDeSalvamentos++;
    }

    @Override
    public Optional<Agendamento> porId(AgendamentoId id) {
        return Optional.ofNullable(agendamentos.get(id));
    }

    @Override
    public List<Agendamento> porCliente(ClienteId clienteId) {
        List<Agendamento> encontrados = new ArrayList<>();
        for (Agendamento agendamento : agendamentos.values()) {
            if (agendamento.getClienteId().equals(clienteId)) {
                encontrados.add(agendamento);
            }
        }
        return encontrados;
    }

    @Override
    public List<Agendamento> porBarbeiroEData(BarbeiroId barbeiroId, LocalDate data) {
        List<Agendamento> encontrados = new ArrayList<>();
        for (Agendamento agendamento : agendamentos.values()) {
            boolean mesmoBarbeiro = agendamento.getBarbeiroId().equals(barbeiroId);
            boolean mesmaData = agendamento.getPeriodo().inicio().toLocalDate().equals(data);
            if (mesmoBarbeiro && mesmaData) {
                encontrados.add(agendamento);
            }
        }
        return encontrados;
    }

    @Override
    public List<Agendamento> porStatus(StatusAgendamento status) {
        List<Agendamento> encontrados = new ArrayList<>();
        for (Agendamento agendamento : agendamentos.values()) {
            if (agendamento.getStatus() == status) {
                encontrados.add(agendamento);
            }
        }
        return encontrados;
    }

    int quantidadeDeSalvamentos() {
        return quantidadeDeSalvamentos;
    }
}
