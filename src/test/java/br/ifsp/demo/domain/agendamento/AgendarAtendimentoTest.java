package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.servico.ServicoId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("US-01 — Agendar atendimento")
class AgendarAtendimentoTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.1 - [OK] Agendamento realizado com sucesso")
    void agendamentoRealizadoComSucesso() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        Agendamento agendamento = Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corte),
                agendaLivre,
                agora
        );

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getPeriodo().inicio()).isEqualTo(inicio);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(30));
    }
}
