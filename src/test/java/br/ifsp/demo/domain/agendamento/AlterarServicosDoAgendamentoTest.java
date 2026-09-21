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

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.2 - [OK] Remoção de serviço recalcula período e valor")
    void remocaoDeServicoRecalculaPeriodoEValor() {
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

        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Marcos Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(50)),
                List.of(corte, barba),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        agendamento.removerItem(barba.getId());

        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(30);
        assertThat(agendamento.valorTotal().valor()).isEqualByComparingTo("40.00");
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(30));
    }


    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.3 - [ERROR] Remoção do último serviço do agendamento")
    void remocaoDoUltimoServicoNaoEPermitida() {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Joao Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        assertThatThrownBy(() -> agendamento.removerItem(corte.getId()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O agendamento deve conter ao menos um serviço");

        assertThat(agendamento.getItens()).hasSize(1);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.4 - [ERROR] Adição de serviço com conflito de horário na agenda do barbeiro")
    void adicaoDeServicoComConflitoDeHorarioNaoEPermitida() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );
        Agendamento meuAgendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Julio Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        LocalDateTime inicioOutro = inicio.plusMinutes(50);
        Agendamento outroAgendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("João Souza", "11912345678"),
                new Periodo(inicioOutro, inicioOutro.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        AgendaDoBarbeiro agendaComConflito = new AgendaDoBarbeiro(
                barbeiroId, inicio.toLocalDate(), List.of(meuAgendamento, outroAgendamento));

        ItemDeServico servicoExtra = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Barba",
                new Dinheiro(new BigDecimal("25.00")),
                30
        );

        assertThatThrownBy(() -> meuAgendamento.adicionarItem(servicoExtra, agendaComConflito))
                .isInstanceOf(HorarioIndisponivelException.class).hasMessage("Horário indisponível para o barbeiro");

        assertThat(meuAgendamento.getItens()).hasSize(1);
        assertThat(meuAgendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(30));
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.5 - [ERROR] Alteração de agendamento não ativo")
    void alteracaoDeAgendamentoNaoAtivoNaoEPermitida() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Maria Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.CANCELADO,
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
        AgendaDoBarbeiro agendaVazia = new AgendaDoBarbeiro(
                new BarbeiroId(UUID.randomUUID()), inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> agendamento.adicionarItem(barba, agendaVazia))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Apenas agendamentos ativos podem ser alterados");

        assertThat(agendamento.getItens()).hasSize(1);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.6 - [ERROR] Inclusão acima do número máximo de serviços permitidos (5)")
    void inclusaoAcimaDoNumeroMaximoDeServicosNaoEPermitida() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        List<ItemDeServico> cincoItens = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cincoItens.add(new ItemDeServico(
                    new ItemId(UUID.randomUUID()),
                    new ServicoId(UUID.randomUUID()),
                    "Serviço " + i,
                    new Dinheiro(new BigDecimal("10.00")),
                    10
            ));
        }

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Maria Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(50)),
                cincoItens,
                StatusAgendamento.AGENDADO,
                0,
                null
        );

        ItemDeServico sextoItem = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Serviço extra",
                new Dinheiro(new BigDecimal("10.00")),
                10
        );
        AgendaDoBarbeiro agendaVazia = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

        assertThatThrownBy(() -> agendamento.adicionarItem(sextoItem, agendaVazia))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Limite de serviços por agendamento atingido");

        assertThat(agendamento.getItens()).hasSize(5);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("3.7 - [ERROR] Inclusão que ultrapassa a duração máxima (250min)")
    void inclusaoQueUltrapassaADuracaoMaximaNaoEPermitida() {
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());

        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                120
        );
        ItemDeServico massagemCapilar = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Massagem capilar",
                new Dinheiro(new BigDecimal("60.00")),
                110
        );

        Agendamento agendamento = Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Maria Silva", "11987654321"),
                new Periodo(inicio, inicio.plusMinutes(230)),
                List.of(corte, massagemCapilar),
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
        AgendaDoBarbeiro agendaVazia = new AgendaDoBarbeiro(barbeiroId, inicio.toLocalDate(), List.of());

      assertThatThrownBy(() -> agendamento.adicionarItem(barba, agendaVazia))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("duração máxima");

        assertThat(agendamento.getItens()).hasSize(2);
        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(230);
    }
}