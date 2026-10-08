package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.ServicoId;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

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
        throw new UnsupportedOperationException("A implementar");
    }
}
