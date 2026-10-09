package br.ifsp.demo.application;

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
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("US-05 — Serviço de reagendamento de atendimento")
class ReagendarAtendimentoServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 8, 10, 0);
    private static final Clock RELOGIO = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);

    private Agendamento agendamentoAtivo(AgendamentoId id, BarbeiroId barbeiroId, LocalDateTime inicio) {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );

        return Agendamento.reconstituir(
                id,
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Cauã", "16999999999"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                StatusAgendamento.AGENDADO,
                0,
                null
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Reagendamento executado com sucesso deve salvar o agendamento")
    void reagendamentoComSucessoDeveSalvarAgendamento() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        LocalDateTime inicioOriginal = AGORA.plusHours(5);
        Agendamento agendamento = agendamentoAtivo(id, barbeiroId, inicioOriginal);

        LocalDateTime novoInicio = AGORA.plusDays(1).withHour(11).withMinute(0);
        LocalDate novaData = novoInicio.toLocalDate();

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));
        when(repository.porBarbeiroEData(eq(barbeiroId), eq(novaData))).thenReturn(List.of());

        ReagendarAtendimentoService service = new ReagendarAtendimentoService(repository, RELOGIO);

        Agendamento resultado = service.reagendar(id, novoInicio);

        assertThat(resultado.getPeriodo().inicio()).isEqualTo(novoInicio);
        verify(repository).salvar(agendamento);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Reagendamento de agendamento inexistente deve ser rejeitado")
    void reagendamentoDeAgendamentoInexistenteDeveSerRejeitado() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.empty());

        ReagendarAtendimentoService service = new ReagendarAtendimentoService(repository, RELOGIO);

        assertThatThrownBy(() -> service.reagendar(id, AGORA.plusDays(1)))
                .isInstanceOf(AgendamentoNaoEncontradoException.class);
    }
}