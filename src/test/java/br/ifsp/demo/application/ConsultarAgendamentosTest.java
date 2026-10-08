package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.ServicoId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("US-02 — Consultar agendamentos")
class ConsultarAgendamentosTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("2.1 - [OK] Listagem de agendamentos do cliente")
    void agendamentosDoClienteDevemVirOrdenadosPeloInicio() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        ClienteId clienteId = new ClienteId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento tarde = agendamentoComInicio(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 16, 0));
        Agendamento manha = agendamentoComInicio(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 9, 0));
        Agendamento outroDia = agendamentoComInicio(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 1, 14, 0));

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porCliente(clienteId)).thenReturn(List.of(tarde, manha, outroDia));
        ConsultarAgendamentosService service = new ConsultarAgendamentosService(repository);

        List<Agendamento> resultado = service.listarDoCliente(clienteId);

        assertThat(resultado).containsExactly(outroDia, manha, tarde);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("2.2 - [OK] Cliente sem agendamentos")
    void clienteSemAgendamentosDeveReceberListaVazia() {
        ClienteId clienteId = new ClienteId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porCliente(clienteId)).thenReturn(List.of());
        ConsultarAgendamentosService service = new ConsultarAgendamentosService(repository);

        List<Agendamento> resultado = service.listarDoCliente(clienteId);

        assertThat(resultado).isEmpty();
    }

    private Agendamento agendamentoComInicio(ClienteId clienteId, BarbeiroId barbeiroId,
                                             ItemDeServico item, LocalDateTime inicio) {
        return Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                clienteId,
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                new Periodo(inicio, inicio.plusMinutes(item.getDuracaoEmMinutos())),
                List.of(item),
                StatusAgendamento.AGENDADO,
                0,
                null
        );
    }
}
