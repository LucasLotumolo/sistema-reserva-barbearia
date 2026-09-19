package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public static Agendamento criar(ClienteId clienteId, BarbeiroId barbeiroId, Contato contato,
                                    LocalDateTime inicio, List<ItemDeServico> itens,
                                    AgendaDoBarbeiro agenda, LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    // As regras de criação dependem do instante em que a reserva foi feita; reconstituir um
    // agendamento existente (repositório JDBC ou fixture de teste em qualquer status) não as reaplica.
    public static Agendamento reconstituir(AgendamentoId id, ClienteId clienteId, BarbeiroId barbeiroId,
                                           Contato contato, Periodo periodo, List<ItemDeServico> itens,
                                           StatusAgendamento status, int quantidadeDeReagendamentos,
                                           Avaliacao avaliacao) {
        return new Agendamento(id, clienteId, barbeiroId, contato, periodo, itens, status,
                quantidadeDeReagendamentos, avaliacao);
    }

    public Dinheiro valorTotal() {
        throw new UnsupportedOperationException("não implementado");
    }

    public int duracaoTotalEmMinutos() {
        throw new UnsupportedOperationException("não implementado");
    }

    public boolean estaAtivo() {
        throw new UnsupportedOperationException("não implementado");
    }

    public void adicionarItem(ItemDeServico item, AgendaDoBarbeiro agenda) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void removerItem(ItemId itemId) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void reagendarPara(LocalDateTime novoInicio, AgendaDoBarbeiro agenda, LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void cancelar(LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void confirmarPresenca(LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public boolean liberarSeNaoConfirmado(LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void avaliar(int nota, LocalDateTime agora) {
        throw new UnsupportedOperationException("não implementado");
    }

    public AgendamentoId getId() {
        return id;
    }

    public ClienteId getClienteId() {
        return clienteId;
    }

    public BarbeiroId getBarbeiroId() {
        return barbeiroId;
    }

    public Contato getContato() {
        return contato;
    }

    public Periodo getPeriodo() {
        return periodo;
    }

    public List<ItemDeServico> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public int getQuantidadeDeReagendamentos() {
        return quantidadeDeReagendamentos;
    }

    public Optional<Avaliacao> getAvaliacao() {
        return Optional.ofNullable(avaliacao);
    }
}
