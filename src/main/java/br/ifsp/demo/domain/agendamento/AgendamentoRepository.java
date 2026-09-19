package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository {
    void salvar(Agendamento agendamento);

    Optional<Agendamento> porId(AgendamentoId id);

    List<Agendamento> porCliente(ClienteId clienteId);

    List<Agendamento> porBarbeiroEData(BarbeiroId barbeiroId, LocalDate data);

    List<Agendamento> porStatus(StatusAgendamento status);
}
