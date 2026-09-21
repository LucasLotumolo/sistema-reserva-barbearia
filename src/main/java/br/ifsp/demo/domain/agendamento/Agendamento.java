package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.exception.RegraDeNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Agendamento {
    private final AgendamentoId id;
    private final ClienteId clienteId;
    private final BarbeiroId barbeiroId;
    private final Contato contato;
    private Periodo periodo;
    private final List<ItemDeServico> itens;
    private StatusAgendamento status;
    private int quantidadeDeReagendamentos;
    private Avaliacao avaliacao;

    private Agendamento(AgendamentoId id, ClienteId clienteId, BarbeiroId barbeiroId, Contato contato,
                        Periodo periodo, List<ItemDeServico> itens, StatusAgendamento status,
                        int quantidadeDeReagendamentos, Avaliacao avaliacao) {
        this.id = id;
        this.clienteId = clienteId;
        this.barbeiroId = barbeiroId;
        this.contato = contato;
        this.periodo = periodo;
        this.itens = new ArrayList<>(itens);
        this.status = status;
        this.quantidadeDeReagendamentos = quantidadeDeReagendamentos;
        this.avaliacao = avaliacao;
    }

    public static Agendamento reconstituir(AgendamentoId id, ClienteId clienteId, BarbeiroId barbeiroId,
                                           Contato contato, Periodo periodo, List<ItemDeServico> itens,
                                           StatusAgendamento status, int quantidadeDeReagendamentos,
                                           Avaliacao avaliacao) {
        return new Agendamento(id, clienteId, barbeiroId, contato, periodo, itens, status,
                quantidadeDeReagendamentos, avaliacao);
    }

    public void adicionarItem(ItemDeServico item, AgendaDoBarbeiro agenda) {
        itens.add(item);
        int duracaoAtualizada = duracaoTotalEmMinutos();
        LocalDateTime novoFim = periodo.inicio().plusMinutes(duracaoAtualizada);
        this.periodo = new Periodo(periodo.inicio(), novoFim);
    }

    public void removerItem(ItemId itemId) {
        if (itens.size() == 1) {
            throw new RegraDeNegocioException("O agendamento deve conter ao menos um serviço");
        }
        ItemDeServico itemParaRemover = null;
        for (ItemDeServico item : itens) {
            if (item.getId().equals(itemId)) {
                itemParaRemover = item;
            }
        }
        itens.remove(itemParaRemover);

        int duracaoAtualizada = duracaoTotalEmMinutos();
        LocalDateTime novoFim = periodo.inicio().plusMinutes(duracaoAtualizada);
        this.periodo = new Periodo(periodo.inicio(), novoFim);
    }

    public int duracaoTotalEmMinutos() {
        int total = 0;
        for (ItemDeServico item : itens) {
            total = total + item.getDuracaoEmMinutos();
        }
        return total;
    }

    public Dinheiro valorTotal() {
        Dinheiro total = new Dinheiro(BigDecimal.ZERO);
        for (ItemDeServico item : itens) {
            total = total.somar(item.getPreco());
        }
        return total;
    }

    public List<ItemDeServico> getItens() {
        return itens;
    }

    public Periodo getPeriodo() {
        return periodo;
    }
}