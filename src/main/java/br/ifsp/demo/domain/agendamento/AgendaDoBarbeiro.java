package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.Periodo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AgendaDoBarbeiro {
    private final BarbeiroId barbeiroId;
    private final LocalDate data;
    private final List<Agendamento> agendamentosDoDia;

    public AgendaDoBarbeiro(BarbeiroId barbeiroId, LocalDate data, List<Agendamento> agendamentosDoDia) {
        this.barbeiroId = barbeiroId;
        this.data = data;
        this.agendamentosDoDia = List.copyOf(agendamentosDoDia);
    }

    public boolean estaLivre(Periodo periodo, AgendamentoId ignorado) {
        throw new UnsupportedOperationException("não implementado");
    }

    public List<Periodo> horariosDisponiveis(int duracaoEmMinutos, LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public BarbeiroId getBarbeiroId() {
        return barbeiroId;
    }

    public LocalDate getData() {
        return data;
    }
}
