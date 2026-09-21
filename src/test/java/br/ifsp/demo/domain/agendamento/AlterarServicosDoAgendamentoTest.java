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

@DisplayName("US-03 — Alterar os serviços do agendamento")
class AlterarServicosDoAgendamentoTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.1 - [OK] Inclusão de serviço recalcula período e valor")
    void inclusaoDeServicoRecalculaPeriodoEValor() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Ricardo Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        ItemDeServico barba = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Barba",
                new Dinheiro(new BigDecimal("25.00")),
                20
        );

        AgendaDoBarbeiro agendaSemConflito = new AgendaDoBarbeiro(
                barbeiroId, inicio.toLocalDate(), List.of());

        agendamento.adicionarItem(barba, agendaSemConflito);

        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(50);
        assertThat(agendamento.valorTotal().valor()).isEqualByComparingTo("65.00");
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(50));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.2 - [OK] Remoção de serviço recalcula período e valor")
    void remocaoDeServicoRecalculaPeriodoEValor() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );
        ItemDeServico barba = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Barba",
                new Dinheiro(new BigDecimal("25.00")),
                20
        );

        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Marcos Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(50)),
                List.of(corte, barba),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        agendamento.removerItem(barba.getId());

        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(30);
        assertThat(agendamento.valorTotal().valor()).isEqualByComparingTo("40.00");
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(30));
    }


    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.3 - [ERROR] Remoção do último serviço do agendamento")
    void remocaoDoUltimoServicoNaoEPermitida() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Joao Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        assertThatThrownBy(() -> agendamento.removerItem(corte.getId()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O agendamento deve conter ao menos um serviço");

        assertThat(agendamento.getItens()).hasSize(1);
    }
}