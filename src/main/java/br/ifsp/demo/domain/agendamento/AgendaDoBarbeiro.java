package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.HorarioDeFuncionamento;
import br.ifsp.demo.domain.comum.Periodo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
    public List<Periodo> horariosDisponiveis(int duracaoEmMinutos, LocalDateTime agora) {
        Periodo expediente = HorarioDeFuncionamento.PADRAO.expedienteDe(data);
        List<Agendamento> ordenados = new ArrayList<>(agendamentosDoDia);
        ordenados.sort(Comparator.comparing(agendamento -> agendamento.getPeriodo().inicio()));

        List<Periodo> livres = new ArrayList<>();
        LocalDateTime inicioDoIntervalo = expediente.inicio();

        for (Agendamento agendamento : ordenados) {
            Periodo ocupado = agendamento.getPeriodo();

            if (ocupado.inicio().isAfter(inicioDoIntervalo)) {
                livres.add(new Periodo(inicioDoIntervalo, ocupado.inicio()));
            }
            if (ocupado.fim().isAfter(inicioDoIntervalo)) {
                inicioDoIntervalo = ocupado.fim();
            }
        }

        if (inicioDoIntervalo.isBefore(expediente.fim())) {
            livres.add(new Periodo(inicioDoIntervalo, expediente.fim()));
        }

        return livres;
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