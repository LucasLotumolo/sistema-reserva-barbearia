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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.2 - [OK] Agendamento com múltiplos serviços")
    void agendamentoComMultiplosServicosSomaDuracaoEValor() {
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

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        Agendamento agendamento = Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corte, barba),
                agendaLivre,
                agora
        );

        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(50);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(50));
        assertThat(agendamento.valorTotal()).isEqualTo(new Dinheiro(new BigDecimal("65.00")));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.3 - [ERROR] Conflito de horário com outro agendamento")
    void conflitoDeHorarioComOutroAgendamentoDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicioExistente = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamentoExistente = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16988888888"),
                new Periodo(inicioExistente, inicioExistente.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );
        AgendaDoBarbeiro agendaComConflito = new AgendaDoBarbeiro(
                barbeiroId, inicioExistente.toLocalDate(), List.of(agendamentoExistente));

        LocalDateTime inicioSobreposto = inicioExistente.plusMinutes(15);

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicioSobreposto,
                List.of(corte),
                agendaComConflito,
                agora
        )).isInstanceOf(HorarioIndisponivelException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.4 - [OK] Encaixe imediatamente após outro atendimento")
    void encaixeImediatamenteAposOutroAtendimentoDeveSerCriado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicioExistente = LocalDateTime.of(2026, 10, 2, 14, 0);
        LocalDateTime fimExistente = inicioExistente.plusMinutes(30);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamentoExistente = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16988888888"),
                new Periodo(inicioExistente, fimExistente),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );
        AgendaDoBarbeiro agenda = new AgendaDoBarbeiro(
                barbeiroId, inicioExistente.toLocalDate(), List.of(agendamentoExistente));

        Agendamento agendamento = Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                fimExistente,
                List.of(corte),
                agenda,
                agora
        );

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getPeriodo().inicio()).isEqualTo(fimExistente);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(fimExistente.plusMinutes(30));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.5 - [ERROR] Agendamento em horário passado")
    void agendamentoEmHorarioPassadoDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 14, 0);
        LocalDateTime inicioPassado = LocalDateTime.of(2026, 10, 1, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicioPassado.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicioPassado,
                List.of(corte),
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Não é possível agendar em horário passado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.6 - [ERROR] Telefone de contato inválido")
    void telefoneComQuantidadeDeDigitosInvalidaDeveSerRejeitado() {
        assertThatThrownBy(() -> new Contato("Lucas", "123"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O telefone deve ser válido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.7 - [OK] Agendamento no limite da antecedência mínima")
    void agendamentoNoLimiteDaAntecedenciaMinimaDeveSerCriado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = agora.plusMinutes(60);
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
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("1.8 - [ERROR] Agendamento sem a antecedência mínima")
    void agendamentoSemAntecedenciaMinimaDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = agora.plusMinutes(59);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corte),
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Agendamento exige no mínimo 60 minutos de antecedência");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[ERROR] - Início exatamente igual ao instante atual")
    void inicioIgualAoInstanteAtualDeveSerRejeitado() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = agora;
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corte),
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Não é possível agendar em horário passado");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[ERROR] Antecedência de 59 minutos e 59 segundos")
    void antecedenciaUmSegundoAbaixoDoLimiteDeveSerRejeitada() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = agora.plusMinutes(59).plusSeconds(59);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corte),
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Agendamento exige no mínimo 60 minutos de antecedência");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[OK] Antecedência de 60 minutos e 1 segundo")
    void antecedenciaUmSegundoAcimaDoLimiteDeveSerAceita() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = agora.plusMinutes(60).plusSeconds(1);
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
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[ERROR] Agendamento sem nenhum serviço")
    void agendamentoSemNenhumServicoDeveSerRejeitado() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(),
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O agendamento deve conter ao menos um serviço");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[OK] Agendamento com 5 serviços (limite máximo)")
    void agendamentoComCincoServicosDeveSerCriado() {
        List<ItemDeServico> cincoItens = List.of(
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Corte", new Dinheiro(new BigDecimal("40.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Barba", new Dinheiro(new BigDecimal("25.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Sobrancelha", new Dinheiro(new BigDecimal("15.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Hidratação", new Dinheiro(new BigDecimal("30.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Pigmentação", new Dinheiro(new BigDecimal("50.00")), 30)
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
                cincoItens,
                agendaLivre,
                agora
        );

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getItens()).hasSize(5);
        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(150);
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[ERROR] Agendamento com 6 serviços (acima do limite máximo)")
    void agendamentoComSeisServicosDeveSerRejeitado() {
        List<ItemDeServico> seisItens = List.of(
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Corte", new Dinheiro(new BigDecimal("40.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Barba", new Dinheiro(new BigDecimal("25.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Sobrancelha", new Dinheiro(new BigDecimal("15.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Hidratação", new Dinheiro(new BigDecimal("30.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Pigmentação", new Dinheiro(new BigDecimal("50.00")), 30),
                new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                        "Massagem", new Dinheiro(new BigDecimal("20.00")), 30)
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                seisItens,
                agendaLivre,
                agora
        )).isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Limite de serviços por agendamento atingido");
    }

    @Test
    @Tag("UnitTest")
    @Tag("Functional")
    @DisplayName("[OK] Duração total de 240 minutos (limite máximo)")
    void duracaoTotalDeDuzentosEQuarentaMinutosDeveSerCriada() {
        ItemDeServico corteLongo = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                120
        );
        ItemDeServico barbaLonga = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Barba",
                new Dinheiro(new BigDecimal("25.00")),
                120
        );

        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        AgendaDoBarbeiro agendaLivre = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        Agendamento agendamento = Agendamento.criar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corteLongo, barbaLonga),
                agendaLivre,
                agora
        );

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(240);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(240));
    }
}
