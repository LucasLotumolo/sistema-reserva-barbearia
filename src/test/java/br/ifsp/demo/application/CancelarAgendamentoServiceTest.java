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
import br.ifsp.demo.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("US-04 — Serviço de cancelamento de agendamento")
class CancelarAgendamentoServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 8, 10, 0);
    private static final Clock RELOGIO = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);

    private Agendamento agendamentoCom(AgendamentoId id, LocalDateTime inicio, StatusAgendamento status) {
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
                new BarbeiroId(UUID.randomUUID()),
                new Contato("Nonashow", "16999999999"),
                new Periodo(inicio, inicio.plusMinutes(30)),
                List.of(corte),
                status,
                0,
                null
        );
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Cancelamento com sucesso deve salvar o agendamento")
    void cancelamentoComSucessoDeveSalvarAgendamento() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        LocalDateTime inicio = AGORA.plusHours(23);
        Agendamento agendamento = agendamentoCom(id, inicio, StatusAgendamento.AGENDADO);

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));

        CancelarAgendamentoService service = new CancelarAgendamentoService(repository, RELOGIO);

        service.cancelar(id);

        verify(repository).salvar(agendamento);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Cancelamento de agendamento inexistente deve ser rejeitado")
    void cancelamentoDeAgendamentoInexistenteDeveSerRejeitado() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.empty());

        CancelarAgendamentoService service = new CancelarAgendamentoService(repository, RELOGIO);

        assertThatThrownBy(() -> service.cancelar(id))
                .isInstanceOf(AgendamentoNaoEncontradoException.class);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Cancelamento de agendamento já iniciado não deve salvar")
    void cancelamentoDeAgendamentoJaIniciadoNaoDeveSalvar() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        LocalDateTime inicio = AGORA.minusMinutes(10);
        Agendamento agendamento = agendamentoCom(id, inicio, StatusAgendamento.AGENDADO);

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));

        CancelarAgendamentoService service = new CancelarAgendamentoService(repository, RELOGIO);

        assertThatThrownBy(() -> service.cancelar(id))
                .isInstanceOf(RegraDeNegocioException.class);

        verify(repository, never()).salvar(any());
    }
}