package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.HorarioDeFuncionamento;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.exception.RegraDeNegocioException;

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
        if (data.isBefore(agora.toLocalDate())) {
            throw new RegraDeNegocioException("Não é possível consultar disponibilidade para datas passadas");
        }
        if (duracaoEmMinutos <= 0) {
            throw new RegraDeNegocioException("A duração dos serviços deve ser positiva");
        }

        Periodo expediente = HorarioDeFuncionamento.PADRAO.expedienteDe(data);
        List<Agendamento> ativos = new ArrayList<>();
        for (Agendamento agendamento : agendamentosDoDia) {
            if (agendamento.estaAtivo()) {
                ativos.add(agendamento);
            }
        }
        ativos.sort(Comparator.comparing(agendamento -> agendamento.getPeriodo().inicio()));

        List<Periodo> livres = new ArrayList<>();
        LocalDateTime inicioDoIntervalo = agora.isAfter(expediente.inicio()) ? agora : expediente.inicio();

        for (Agendamento agendamento : ativos) {
            Periodo ocupado = agendamento.getPeriodo();

            if (ocupado.inicio().isAfter(inicioDoIntervalo)) {
                adicionarSeComporta(livres, new Periodo(inicioDoIntervalo, ocupado.inicio()), duracaoEmMinutos);
            }
            if (ocupado.fim().isAfter(inicioDoIntervalo)) {
                inicioDoIntervalo = ocupado.fim();
            }
        }

        if (inicioDoIntervalo.isBefore(expediente.fim())) {
            adicionarSeComporta(livres, new Periodo(inicioDoIntervalo, expediente.fim()), duracaoEmMinutos);
        }

        return livres;
    }

    private void adicionarSeComporta(List<Periodo> livres, Periodo intervalo, int duracaoEmMinutos) {
        if (intervalo.duracaoEmMinutos() < duracaoEmMinutos) {
            return;
        }

        livres.add(intervalo);
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