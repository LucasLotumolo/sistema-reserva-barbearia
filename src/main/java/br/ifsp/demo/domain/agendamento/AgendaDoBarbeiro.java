package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.Periodo;

import java.time.LocalDate;
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
    public boolean estaLivre(Periodo periodo, AgendamentoId agendamentoAtual) {
        boolean livre = true;
        for (Agendamento agendamento : agendamentosDoDia) {
            boolean ehOMesmoAgendamento = agendamento.getId().equals(agendamentoAtual);
            boolean estaCancelado = agendamento.getStatus() == StatusAgendamento.CANCELADO;

            if (!ehOMesmoAgendamento && !estaCancelado) {
                if (agendamento.getPeriodo().sobrepoe(periodo)) {
                    livre = false;
                }
            }
        }
        return livre;
    }
}