package br.ifsp.demo.service;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("US-07 — Serviço de consulta de horários disponíveis")
class ConsultarHorariosDisponiveisServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DATA = LocalDate.of(2026, 10, 8);
    private static final Clock RELOGIO = Clock.fixed(
            LocalDateTime.of(2026, 10, 7, 10, 0).atZone(FUSO).toInstant(), FUSO);
    private static final BarbeiroId BARBEIRO = new BarbeiroId(UUID.randomUUID());

    private static final Servico CORTE = servico("Corte", 30);
    private static final Servico BARBA = servico("Barba", 45);
    private static final CatalogoDeServicos CATALOGO = id ->
            Optional.ofNullable(Map.of(CORTE.id(), CORTE, BARBA.id(), BARBA).get(id));

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Consulta com agenda parcialmente ocupada considera a soma das durações dos serviços")
    void consultaDeveUsarASomaDasDuracoesDosServicosDesejados() {
        Agendamento ocupado = agendamentoDas(10, 0, 11, 0);
        AgendamentoRepository repositorio = (barbeiro, data) -> List.of(ocupado);
        ConsultarHorariosDisponiveisService service =
                new ConsultarHorariosDisponiveisService(repositorio, CATALOGO, RELOGIO);

        List<Periodo> disponiveis = service.consultar(BARBEIRO, DATA, List.of(CORTE.id(), BARBA.id()));

        assertThat(disponiveis).containsExactly(new Periodo(DATA.atTime(11, 0), DATA.atTime(19, 0)));
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US07-CT13 — Serviço fora do catálogo é rejeitado")
    void servicoForaDoCatalogoDeveSerRejeitado() {
        ConsultarHorariosDisponiveisService service = serviceComAgenda();
        ServicoId inexistente = new ServicoId(UUID.randomUUID());

        assertThatThrownBy(() -> service.consultar(BARBEIRO, DATA, List.of(CORTE.id(), inexistente)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Serviço não encontrado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US07-CT14 — Consulta sem serviços é rejeitada")
    void consultaSemServicosDeveSerRejeitada() {
        ConsultarHorariosDisponiveisService service = serviceComAgenda();

        assertThatThrownBy(() -> service.consultar(BARBEIRO, DATA, List.of()))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("US07-CT15 — Intervalo livre igual à soma das durações dos serviços é devolvido")
    void intervaloIgualASomaDasDuracoesDeveSerDevolvido() {
        ConsultarHorariosDisponiveisService service = serviceComAgenda(
                agendamentoDas(9, 0, 10, 0), agendamentoDas(11, 15, 19, 0));

        List<Periodo> disponiveis = service.consultar(BARBEIRO, DATA, List.of(CORTE.id(), BARBA.id()));

        assertThat(disponiveis).containsExactly(new Periodo(DATA.atTime(10, 0), DATA.atTime(11, 15)));
    }

    private static ConsultarHorariosDisponiveisService serviceComAgenda(Agendamento... agendamentos) {
        AgendamentoRepository repositorio = (barbeiro, data) -> List.of(agendamentos);
        return new ConsultarHorariosDisponiveisService(repositorio, CATALOGO, RELOGIO);
    }

    private static Servico servico(String nome, int duracaoEmMinutos) {
        return new Servico(new ServicoId(UUID.randomUUID()), nome, new Dinheiro(new BigDecimal("40.00")),
                duracaoEmMinutos);
    }

    private static Agendamento agendamentoDas(int horaInicio, int minutoInicio, int horaFim, int minutoFim) {
        Periodo periodo = new Periodo(DATA.atTime(horaInicio, minutoInicio), DATA.atTime(horaFim, minutoFim));
        ItemDeServico item = new ItemDeServico(new ItemId(UUID.randomUUID()), CORTE.id(), CORTE.nome(),
                CORTE.preco(), periodo.duracaoEmMinutos());

        return Agendamento.reconstituir(new AgendamentoId(UUID.randomUUID()), new ClienteId(UUID.randomUUID()),
                BARBEIRO, new Contato("Herick", "16999999999"), periodo, List.of(item),
                StatusAgendamento.AGENDADO, 0, null);
    }
}
