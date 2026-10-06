package br.ifsp.demo.service;

import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.ServicoId;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class ConsultarHorariosDisponiveisService {
    private final AgendamentoRepository agendamentoRepository;
    private final CatalogoDeServicos catalogoDeServicos;
    private final Clock clock;

    public ConsultarHorariosDisponiveisService(AgendamentoRepository agendamentoRepository,
                                               CatalogoDeServicos catalogoDeServicos, Clock clock) {
        this.agendamentoRepository = agendamentoRepository;
        this.catalogoDeServicos = catalogoDeServicos;
        this.clock = clock;
    }

    public List<Periodo> consultar(BarbeiroId barbeiroId, LocalDate data, List<ServicoId> servicos) {
        throw new UnsupportedOperationException("não implementado");
    }
}
