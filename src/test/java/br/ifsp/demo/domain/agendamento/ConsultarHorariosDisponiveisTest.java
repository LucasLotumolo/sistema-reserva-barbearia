package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.Periodo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("US-07 — Consultar horários disponíveis")
class ConsultarHorariosDisponiveisTest {

    private static final int DURACAO_DO_CORTE_EM_MINUTOS = 30;

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Consulta de dia sem agendamentos")
    void consultaDeDiaSemAgendamentosDeveRetornarTodoOExpediente() {
        LocalDate data = LocalDate.of(2026, 10, 8);
        LocalDateTime agora = LocalDateTime.of(2026, 10, 7, 10, 0);
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(new BarbeiroId(UUID.randomUUID()), data, List.of());

        List<Periodo> disponiveis = agenda.horariosDisponiveis(DURACAO_DO_CORTE_EM_MINUTOS, agora);

        assertThat(disponiveis).containsExactly(new Periodo(data.atTime(9, 0), data.atTime(19, 0)));
    }
}
