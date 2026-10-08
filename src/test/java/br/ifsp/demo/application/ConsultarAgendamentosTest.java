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
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("2.3 - [ERROR] Consulta de agendamento inexistente")
    void consultaDeAgendamentoInexistenteDeveSerRejeitada() {
        AgendamentoId idInexistente = new AgendamentoId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(idInexistente)).thenReturn(Optional.empty());
        ConsultarAgendamentosService service = new ConsultarAgendamentosService(repository);

        assertThatThrownBy(() -> service.buscarPorId(idInexistente))
                .isInstanceOf(AgendamentoNaoEncontradoException.class)
                .hasMessage("Agendamento não encontrado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("2.4 - [OK] Consulta da ocupação do barbeiro por data")
    void ocupacaoDoBarbeiroDeveRetornarSomenteOsPeriodosDaData() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        ClienteId clienteId = new ClienteId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        LocalDate dia = LocalDate.of(2026, 10, 2);
        LocalDate outroDia = LocalDate.of(2026, 10, 3);

        Agendamento tarde = agendamentoComInicio(clienteId, barbeiroId, corte, dia.atTime(16, 0));
        Agendamento manha = agendamentoComInicio(clienteId, barbeiroId, corte, dia.atTime(9, 0));
        Agendamento emOutroDia = agendamentoComInicio(clienteId, barbeiroId, corte, outroDia.atTime(14, 0));

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porBarbeiroEData(barbeiroId, dia)).thenReturn(List.of(tarde, manha));
        when(repository.porBarbeiroEData(barbeiroId, outroDia)).thenReturn(List.of(emOutroDia));
        ConsultarAgendamentosService service = new ConsultarAgendamentosService(repository);

        List<Periodo> resultado = service.horariosOcupados(barbeiroId, dia);

        assertThat(resultado).containsExactly(manha.getPeriodo(), tarde.getPeriodo());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("2.5 - [OK] Agendamentos encerrados fora da lista de futuros")
    void listaDeFuturosDeveConterApenasAgendamentosAtivos() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        ClienteId clienteId = new ClienteId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendado = agendamentoComStatus(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 9, 0), StatusAgendamento.AGENDADO);
        Agendamento confirmado = agendamentoComStatus(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 11, 0), StatusAgendamento.CONFIRMADO);
        Agendamento cancelado = agendamentoComStatus(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 14, 0), StatusAgendamento.CANCELADO);
        Agendamento expirado = agendamentoComStatus(clienteId, barbeiroId, corte,
                LocalDateTime.of(2026, 10, 2, 16, 0), StatusAgendamento.EXPIRADO);

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porCliente(clienteId))
                .thenReturn(List.of(expirado, confirmado, cancelado, agendado));
        ConsultarAgendamentosService service = new ConsultarAgendamentosService(repository);

        List<Agendamento> resultado = service.listarFuturosDoCliente(clienteId);

        assertThat(resultado).containsExactly(agendado, confirmado);
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

    private Agendamento agendamentoComStatus(ClienteId clienteId, BarbeiroId barbeiroId,
                                             ItemDeServico item, LocalDateTime inicio,
                                             StatusAgendamento status) {
        return Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                clienteId,
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                new Periodo(inicio, inicio.plusMinutes(item.getDuracaoEmMinutos())),
                List.of(item),
                status,
                0,
                null
        );
    }
}
