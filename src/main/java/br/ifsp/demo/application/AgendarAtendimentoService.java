package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.AgendaDoBarbeiro;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.ServicoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgendarAtendimentoService {
    private final AgendamentoRepository repository;
    private final CatalogoDeServicos catalogo;
    private final Clock clock;

    public AgendarAtendimentoService(AgendamentoRepository repository, CatalogoDeServicos catalogo, Clock clock) {
        this.repository = repository;
        this.catalogo = catalogo;
        this.clock = clock;
    }

    public Agendamento agendar(ClienteId clienteId, BarbeiroId barbeiroId, Contato contato,
                               LocalDateTime inicio, List<ServicoId> servicos) {
        List<ItemDeServico> itens = new ArrayList<>();
        for (ServicoId servicoId : servicos) {
            Servico servico = catalogo.porId(servicoId)
                    .orElseThrow(() -> new ServicoNaoEncontradoException("Serviço não encontrado"));
            itens.add(new ItemDeServico(new ItemId(UUID.randomUUID()), servico.id(),
                    servico.nome(), servico.preco(), servico.duracaoEmMinutos()));
        }

        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(),
                repository.porBarbeiroEData(barbeiroId, inicio.toLocalDate()));

        Agendamento agendamento = Agendamento.criar(clienteId, barbeiroId, contato, inicio, itens,
                agenda, LocalDateTime.now(clock));

        repository.salvar(agendamento);
        return agendamento;
    }
}
