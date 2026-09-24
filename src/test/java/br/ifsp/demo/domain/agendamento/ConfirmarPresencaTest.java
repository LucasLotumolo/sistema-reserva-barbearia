package br.ifsp.demo.domain.agendamento;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.ifsp.demo.exception.RegraDeNegocioException;

@DisplayName("US-06 — Confirmar presença")
class ConfirmarPresencaTest {

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Confirmação realizada com sucesso")
    void confirmacaoRealizadaComSucessoDeveAtualizarStatus() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicio = agora.plusHours(2);
        Periodo periodo = new Periodo(inicio, inicio.plusMinutes(30));

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Cauã", "16999999999"),
                periodo,
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        agendamento.confirmarPresenca(agora);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
        assertThat(agendamento.getPeriodo()).isEqualTo(periodo);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Confirmação antes da abertura da janela")
    void confirmacaoAntesDaAberturaDaJanelaDeveSerRejeitada() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicio = agora.plusHours(25);
        Periodo periodo = new Periodo(inicio, inicio.plusMinutes(30));

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Cauã", "16999999999"),
                periodo,
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        assertThatThrownBy(() -> agendamento.confirmarPresenca(agora))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("A confirmacao so e liberada 24 horas antes do atendimento");

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Confirmação exatamente na abertura da janela")
    void confirmacaoExatamenteNaAberturaDaJanelaDeveSerAceita() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 9, 21, 10, 0);
        LocalDateTime inicio = agora.plusHours(24);
        Periodo periodo = new Periodo(inicio, inicio.plusMinutes(30));

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Cauã", "16999999999"),
                periodo,
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        agendamento.confirmarPresenca(agora);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CONFIRMADO);
    }

}