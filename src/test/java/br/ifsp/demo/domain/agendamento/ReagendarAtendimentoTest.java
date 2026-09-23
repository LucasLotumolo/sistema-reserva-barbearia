package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.ServicoId;

import br.ifsp.demo.exception.HorarioIndisponivelException;
import br.ifsp.demo.exception.RegraDeNegocioException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Reagendar atendimento")
class ReagendarAtendimentoTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Reagendamento realizado com sucesso")
    void reagendamentoRealizadoComSucesso() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicioOriginal = agora.plusHours(5); // com antecedência suficiente
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16999999999"),
                new Periodo(inicioOriginal, inicioOriginal.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        List<ItemDeServico> itensOriginais = new ArrayList<>(agendamento.getItens());
        Dinheiro valorOriginal = agendamento.valorTotal();

        LocalDateTime novoInicio = agora.plusDays(1).withHour(11).withMinute(0);
        AgendaDoBarbeiro agendaComBarbeiroLivre = new AgendaDoBarbeiro(
                barbeiroId, novoInicio.toLocalDate(), List.of());

        agendamento.reagendarPara(novoInicio, agendaComBarbeiroLivre, agora);

        assertThat(agendamento.getPeriodo().inicio()).isEqualTo(novoInicio);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(novoInicio.plusMinutes(30));
        assertThat(agendamento.getItens()).isEqualTo(itensOriginais);
        assertThat(agendamento.valorTotal()).isEqualTo(valorOriginal);
        assertThat(agendamento.getQuantidadeDeReagendamentos()).isEqualTo(1);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Reagendamento para horário ocupado")
    void reagendamentoParaHorarioOcupadoDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicioOriginal = agora.plusHours(5);
        Periodo periodoOriginal = new Periodo(inicioOriginal, inicioOriginal.plusMinutes(30));
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16999999999"),
                periodoOriginal,
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        LocalDateTime novoInicio = agora.plusDays(1).withHour(11).withMinute(0);

        ItemDeServico outroCorte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );
        Agendamento agendamentoConflitante = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Outro Cliente", "16988888888"),
                new Periodo(novoInicio.minusMinutes(15), novoInicio.plusMinutes(15)),
                List.of(outroCorte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        AgendaDoBarbeiro agendaComConflito = new AgendaDoBarbeiro(
                barbeiroId, novoInicio.toLocalDate(), List.of(agendamentoConflitante));

        assertThatThrownBy(() -> agendamento.reagendarPara(novoInicio, agendaComConflito, agora))
                .isInstanceOf(HorarioIndisponivelException.class);

        assertThat(agendamento.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(agendamento.getQuantidadeDeReagendamentos()).isEqualTo(0);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Reagendamento sem antecedência mínima")
    void reagendamentoSemAntecedenciaMinimaDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicioOriginal = agora.plusMinutes(60);
        Periodo periodoOriginal = new Periodo(inicioOriginal, inicioOriginal.plusMinutes(30));
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16999999999"),
                periodoOriginal,
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        LocalDateTime novoInicio = agora.plusDays(1).withHour(11).withMinute(0);
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(barbeiroId, novoInicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> agendamento.reagendarPara(novoInicio, agenda, agora))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("120");

        assertThat(agendamento.getPeriodo()).isEqualTo(periodoOriginal);
        assertThat(agendamento.getQuantidadeDeReagendamentos()).isEqualTo(0);
    }
}