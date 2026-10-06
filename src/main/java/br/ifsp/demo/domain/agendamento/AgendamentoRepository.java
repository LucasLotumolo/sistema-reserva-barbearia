package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository {
    List<Agendamento> porBarbeiroEData(BarbeiroId barbeiroId, LocalDate data);
}
