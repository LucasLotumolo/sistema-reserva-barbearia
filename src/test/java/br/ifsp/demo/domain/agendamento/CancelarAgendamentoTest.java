package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("US-04 — Cancelar agendamento")
class CancelarAgendamentoTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("4.1 - [OK] Cancelamento de agendamento antes do início")
    void cancelamentoDeAgendamentoAntesDoInicioAlteraStatus() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        LocalDateTime horarioDoCancelamento = inicio.minusHours(2);

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Kleiton Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        agendamento.cancelar(horarioDoCancelamento);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("4.2 - [OK] Cancelamento de agendamento confirmado antes do início")
    void cancelamentoDeAgendamentoConfirmadoAntesDoInicioAlteraStatus() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        LocalDateTime horarioDoCancelamento = inicio.minusHours(2);

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Julio Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.CONFIRMADO,
                0,
                null
        );

        agendamento.cancelar(horarioDoCancelamento);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
    }


    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("4.3 - [OK] Horário de agendamento cancelado fica disponível")
    void horarioDeAgendamentoCanceladoFicaDisponivel() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        Agendamento agendamentoCancelado = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Flower Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.CANCELADO,
                0,
                null
        );

        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(
                barbeiroId, inicio.toLocalDate(), List.of(agendamentoCancelado));

        Periodo periodoDesejadoPorOutroCliente = new Periodo(inicio, inicio.plusMinutes(30));

        boolean disponivel = agenda.estaLivre(periodoDesejadoPorOutroCliente, null);

        assertThat(disponivel).isTrue();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("4.4 - [ERROR] Cancelamento após o início do atendimento")
    void cancelamentoAposOInicioNaoEPermitido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        LocalDateTime horarioDoCancelamento = inicio.plusMinutes(10);

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Blower Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        assertThatThrownBy(() -> agendamento.cancelar(horarioDoCancelamento))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("já foi iniciado");

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
    }
}