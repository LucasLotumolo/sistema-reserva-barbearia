package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;

public class ItemDeServico {
    private final ItemId id;
    private final ServicoId servicoId;
    private final String nomeDoServico;
    private final Dinheiro preco;
    private final int duracaoEmMinutos;

    public ItemDeServico(ItemId id, ServicoId servicoId, String nomeDoServico, Dinheiro preco, int duracaoEmMinutos) {
        this.id = id;
        this.servicoId = servicoId;
        this.nomeDoServico = nomeDoServico;
        this.preco = preco;
        this.duracaoEmMinutos = duracaoEmMinutos;
    }

    public static ItemDeServico de(Servico servico) {
        throw new UnsupportedOperationException("não implementado");
    }

    public ItemId getId() {
        return id;
    }

    public ServicoId getServicoId() {
        return servicoId;
    }

    public String getNomeDoServico() {
        return nomeDoServico;
    }

    public Dinheiro getPreco() {
        return preco;
    }

    public int getDuracaoEmMinutos() {
        return duracaoEmMinutos;
    }
}
