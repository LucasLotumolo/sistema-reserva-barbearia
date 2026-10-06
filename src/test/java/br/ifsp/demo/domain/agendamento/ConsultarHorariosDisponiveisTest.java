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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("US-07 — Consultar horários disponíveis")
class ConsultarHorariosDisponiveisTest {

    private static final int DURACAO_DO_CORTE_EM_MINUTOS = 30;
    private static final LocalDate DATA = LocalDate.of(2026, 10, 8);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 7, 10, 0);
    private static final BarbeiroId BARBEIRO = new BarbeiroId(UUID.randomUUID());

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Consulta de dia sem agendamentos")
    void consultaDeDiaSemAgendamentosDeveRetornarTodoOExpediente() {
        AgendaDoBarbeiro agenda = agendaCom();

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).containsExactly(periodoDas(9, 0, 19, 0));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Consulta com agenda parcialmente ocupada")
    void consultaComAgendaParcialmenteOcupadaDeveRetornarApenasOsIntervalosLivres() {
        Agendamento ocupado = agendamentoDas(10, 0, 11, 0, StatusAgendamento.AGENDADO);
        AgendaDoBarbeiro agenda = agendaCom(ocupado);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).containsExactly(
                periodoDas(9, 0, 10, 0),
                periodoDas(11, 0, 19, 0)
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Intervalo livre menor que a duração dos serviços")
    void intervaloLivreMenorQueADuracaoNaoDeveSerRetornado() {
        Agendamento manha = agendamentoDas(9, 0, 10, 0, StatusAgendamento.AGENDADO);
        Agendamento tarde = agendamentoDas(10, 20, 19, 0, StatusAgendamento.AGENDADO);
        AgendaDoBarbeiro agenda = agendaCom(manha, tarde);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).isEmpty();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Agendamentos encerrados não bloqueiam a disponibilidade")
    void agendamentosCanceladosEExpiradosDevemLiberarOHorario() {
        Agendamento cancelado = agendamentoDas(10, 0, 11, 0, StatusAgendamento.CANCELADO);
        Agendamento expirado = agendamentoDas(14, 0, 15, 0, StatusAgendamento.EXPIRADO);
        AgendaDoBarbeiro agenda = agendaCom(cancelado, expirado);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).containsExactly(periodoDas(9, 0, 19, 0));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Consulta para data passada")
    void consultaParaDataPassadaDeveSerRejeitada() {
        LocalDate ontem = AGORA.toLocalDate().minusDays(1);
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(BARBEIRO, ontem, List.of());

        assertThatThrownBy(() -> agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("datas passadas");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Disponibilidade de encaixe entre atendimentos")
    void periodoQueComecaNoTerminoDeOutroAtendimentoDeveEstarDisponivel() {
        Agendamento manha = agendamentoDas(9, 0, 10, 0, StatusAgendamento.AGENDADO);
        AgendaDoBarbeiro agenda = agendaCom(manha);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).containsExactly(periodoDas(10, 0, 19, 0));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Intervalo livre exatamente igual à duração dos serviços")
    void intervaloLivreIgualADuracaoDeveSerRetornado() {
        Agendamento manha = agendamentoDas(9, 0, 10, 0, StatusAgendamento.AGENDADO);
        Agendamento tarde = agendamentoDas(10, 30, 19, 0, StatusAgendamento.AGENDADO);
        AgendaDoBarbeiro agenda = agendaCom(manha, tarde);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).containsExactly(periodoDas(10, 0, 10, 30));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Consulta de dia completamente ocupado")
    void consultaDeDiaCompletamenteOcupadoNaoDeveRetornarHorarios() {
        Agendamento diaInteiro = agendamentoDas(9, 0, 19, 0, StatusAgendamento.AGENDADO);
        AgendaDoBarbeiro agenda = agendaCom(diaInteiro);

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, AGORA);

        assertThat(disponiveis).isEmpty();
    }

    private AgendaDoBarbeiro agendaCom(Agendamento... agendamentos) {
        return new AgendaDoBarbeiro(BARBEIRO, DATA, List.of(agendamentos));
    }

    private Periodo periodoDas(int horaInicio, int minutoInicio, int horaFim, int minutoFim) {
        return new Periodo(DATA.atTime(horaInicio, minutoInicio), DATA.atTime(horaFim, minutoFim));
    }

    private Agendamento agendamentoDas(int horaInicio, int minutoInicio, int horaFim, int minutoFim,
                                       StatusAgendamento status) {
        Periodo periodo = periodoDas(horaInicio, minutoInicio, horaFim, minutoFim);
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                periodo.duracaoEmMinutos()
        );

        return Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                BARBEIRO,
                new Contato("Herick", "16999999999"),
                periodo,
                List.of(corte),
                status,
                0,
                null
        );
    }
}
