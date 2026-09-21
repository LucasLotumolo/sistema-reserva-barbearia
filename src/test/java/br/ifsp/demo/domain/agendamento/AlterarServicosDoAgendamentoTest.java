package br.ifsp.demo.domain.agendamento;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
}