package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.Avaliacao;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("US-08 — Serviço de avaliação de atendimento")
class AvaliarAtendimentoServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DATA = LocalDate.of(2026, 10, 8);
    private static final Periodo PERIODO = new Periodo(DATA.atTime(10, 0), DATA.atTime(10, 30));
    private static final LocalDateTime APOS_O_TERMINO = DATA.atTime(11, 0);
    private static final Clock RELOGIO = Clock.fixed(APOS_O_TERMINO.atZone(FUSO).toInstant(), FUSO);

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Avaliação realizada com sucesso é registrada e salva")
    void avaliacaoDeveSerRegistradaESalvaNoRepositorio() {
        Agendamento agendamento = agendamentoCom(StatusAgendamento.CONFIRMADO);
        AgendamentoRepositoryEmMemoria repositorio = new AgendamentoRepositoryEmMemoria(agendamento);
        AvaliarAtendimentoService service = new AvaliarAtendimentoService(repositorio, RELOGIO);

        service.avaliar(agendamento.getId(), 5);

        Agendamento salvo = repositorio.porId(agendamento.getId()).orElseThrow();
        assertThat(salvo.getAvaliacao()).isEqualTo(new Avaliacao(5, APOS_O_TERMINO));
        assertThat(repositorio.quantidadeDeSalvamentos()).isEqualTo(1);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US08-CT11 — Avaliação de agendamento inexistente é rejeitada")
    void avaliacaoDeAgendamentoInexistenteDeveSerRejeitada() {
        AvaliarAtendimentoService service =
                new AvaliarAtendimentoService(new AgendamentoRepositoryEmMemoria(), RELOGIO);
        AgendamentoId inexistente = new AgendamentoId(UUID.randomUUID());

        assertThatThrownBy(() -> service.avaliar(inexistente, 5))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Agendamento não encontrado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US08-CT12 — Avaliação rejeitada não é salva")
    void avaliacaoRejeitadaNaoDeveSerSalva() {
        Agendamento agendamento = agendamentoCom(StatusAgendamento.CONFIRMADO);
        AgendamentoRepositoryEmMemoria repositorio = new AgendamentoRepositoryEmMemoria(agendamento);
        AvaliarAtendimentoService service = new AvaliarAtendimentoService(repositorio, RELOGIO);

        assertThatThrownBy(() -> service.avaliar(agendamento.getId(), 0))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThat(repositorio.quantidadeDeSalvamentos()).isZero();
    }

    private static Agendamento agendamentoCom(StatusAgendamento status) {
        ItemDeServico corte = new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                "Corte", new Dinheiro(new BigDecimal("40.00")), PERIODO.duracaoEmMinutos());

        return Agendamento.reconstituir(new AgendamentoId(UUID.randomUUID()), new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()), new Contato("Herick", "16999999999"), PERIODO, List.of(corte),
                status, 0, null);
    }
}
