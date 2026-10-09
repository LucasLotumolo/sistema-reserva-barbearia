package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.AgendaDoBarbeiro;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import br.ifsp.demo.exception.ServicoNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AlterarServicosDoAgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final CatalogoDeServicos catalogoDeServicos;

    public AlterarServicosDoAgendamentoService(AgendamentoRepository agendamentoRepository,
                                               CatalogoDeServicos catalogoDeServicos) {
        this.agendamentoRepository = agendamentoRepository;
        this.catalogoDeServicos = catalogoDeServicos;
    }

    public void adicionarServico(AgendamentoId agendamentoId, ServicoId servicoId) {
        Agendamento agendamento = agendamentoRepository.porId(agendamentoId)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        Servico servico = catalogoDeServicos.porId(servicoId)
                .orElseThrow(() -> new ServicoNaoEncontradoException("Serviço não encontrado"));

        ItemDeServico item = new ItemDeServico(new ItemId(UUID.randomUUID()), servico.id(),
                servico.nome(), servico.preco(), servico.duracaoEmMinutos());

        LocalDate data = agendamento.getPeriodo().inicio().toLocalDate();
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(agendamento.getBarbeiroId(), data,
                agendamentoRepository.porBarbeiroEData(agendamento.getBarbeiroId(), data));

        agendamento.adicionarItem(item, agenda);
        agendamentoRepository.salvar(agendamento);
    }

    public void removerServico(AgendamentoId agendamentoId, ItemId itemId) {
        Agendamento agendamento = agendamentoRepository.porId(agendamentoId)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Agendamento não encontrado"));

        agendamento.removerItem(itemId);
        agendamentoRepository.salvar(agendamento);
    }
}