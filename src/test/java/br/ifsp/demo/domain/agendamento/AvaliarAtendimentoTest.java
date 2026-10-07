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

@DisplayName("US-08 — Avaliar atendimento")
class AvaliarAtendimentoTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 8);
    private static final Periodo PERIODO = new Periodo(DATA.atTime(10, 0), DATA.atTime(10, 30));
    private static final LocalDateTime APOS_O_TERMINO = DATA.atTime(11, 0);

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Avaliação realizada com sucesso")
    void avaliacaoDeAtendimentoConfirmadoETerminadoDeveSerRegistrada() {
        Agendamento agendamento = agendamentoCom(StatusAgendamento.CONFIRMADO);

        agendamento.avaliar(4, APOS_O_TERMINO);

        assertThat(agendamento.getAvaliacao()).isEqualTo(new Avaliacao(4, APOS_O_TERMINO));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Avaliação com nota nos limites da escala")
    void avaliacaoComNotaMinimaOuMaximaDeveSerRegistrada() {
        Agendamento comNotaMinima = agendamentoCom(StatusAgendamento.CONFIRMADO);
        Agendamento comNotaMaxima = agendamentoCom(StatusAgendamento.CONFIRMADO);

        comNotaMinima.avaliar(1, APOS_O_TERMINO);
        comNotaMaxima.avaliar(5, APOS_O_TERMINO);

        assertThat(comNotaMinima.getAvaliacao().nota()).isEqualTo(1);
        assertThat(comNotaMaxima.getAvaliacao().nota()).isEqualTo(5);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Nota abaixo do mínimo")
    void notaAbaixoDoMinimoDeveSerRejeitada() {
        Agendamento agendamento = agendamentoCom(StatusAgendamento.CONFIRMADO);

        assertThatThrownBy(() -> agendamento.avaliar(0, APOS_O_TERMINO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("entre 1 e 5");
        assertThat(agendamento.getAvaliacao()).isNull();
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Nota acima do máximo")
    void notaAcimaDoMaximoDeveSerRejeitada() {
        Agendamento agendamento = agendamentoCom(StatusAgendamento.CONFIRMADO);

        assertThatThrownBy(() -> agendamento.avaliar(6, APOS_O_TERMINO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("entre 1 e 5");
        assertThat(agendamento.getAvaliacao()).isNull();
    }

    private Agendamento agendamentoCom(StatusAgendamento status) {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                PERIODO.duracaoEmMinutos()
        );

        return Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Herick", "16999999999"),
                PERIODO,
                List.of(corte),
                status,
                0,
                null
        );
    }
}
