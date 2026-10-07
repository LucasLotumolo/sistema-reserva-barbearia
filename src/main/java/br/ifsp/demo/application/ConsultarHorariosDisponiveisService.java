package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.AgendaDoBarbeiro;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.RegraDeNegocioException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        int duracaoTotalEmMinutos = 0;
        for (ServicoId servicoId : servicos) {
            Servico servico = catalogoDeServicos.porId(servicoId)
                    .orElseThrow(() -> new RegraDeNegocioException("Serviço não encontrado: " + servicoId.valor()));
            duracaoTotalEmMinutos += servico.duracaoEmMinutos();
        }

        List<Agendamento> agendamentosDoDia = agendamentoRepository.porBarbeiroEData(barbeiroId, data);
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(barbeiroId, data, agendamentosDoDia);

        return agenda.horariosDisponiveis(duracaoTotalEmMinutos, LocalDateTime.now(clock));
    }
}
