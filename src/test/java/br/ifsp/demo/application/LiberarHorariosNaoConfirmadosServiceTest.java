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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("US-06 — Serviço de liberação de horários não confirmados")
class LiberarHorariosNaoConfirmadosServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 8, 10, 0);
    private static final Clock RELOGIO = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);

    private Agendamento agendamentoAgendadoComInicio(LocalDateTime inicio) {
        ItemDeServico corte = new ItemDeServico(
                new ItemId(UUID.randomUUID()),
                new ServicoId(UUID.randomUUID()),
                "Corte",
                new Dinheiro(new BigDecimal("40.00")),
                30
        );
        return Agendamento.reconstituir(
                new AgendamentoId(UUID.randomUUID()),
                new ClienteId(UUID.randomUUID()),
                new BarbeiroId(UUID.randomUUID()),
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
    @DisplayName("[OK] Libera agendamentos cujo prazo de confirmacao se encerrou")
    void liberaAgendamentosComPrazoEncerrado() {
        Agendamento expirado = agendamentoAgendadoComInicio(AGORA.plusMinutes(20));

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porStatus(StatusAgendamento.AGENDADO)).thenReturn(List.of(expirado));

        LiberarHorariosNaoConfirmadosService service = new LiberarHorariosNaoConfirmadosService(repository, RELOGIO);

        int liberados = service.liberarHorariosNaoConfirmados();

        assertThat(liberados).isEqualTo(1);
        assertThat(expirado.getStatus()).isEqualTo(StatusAgendamento.EXPIRADO);
        verify(repository).salvar(expirado);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Nao libera agendamentos ainda dentro do prazo de confirmacao")
    void naoLiberaAgendamentosDentroDoPrazo() {
        Agendamento dentroDoPrazo = agendamentoAgendadoComInicio(AGORA.plusHours(2));

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porStatus(StatusAgendamento.AGENDADO)).thenReturn(List.of(dentroDoPrazo));

        LiberarHorariosNaoConfirmadosService service = new LiberarHorariosNaoConfirmadosService(repository, RELOGIO);

        int liberados = service.liberarHorariosNaoConfirmados();

        assertThat(liberados).isEqualTo(0);
        assertThat(dentroDoPrazo.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        verify(repository, never()).salvar(any());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Libera apenas os agendamentos expirados entre varios")
    void liberaApenasOsAgendamentosExpiradosEntreVarios() {
        Agendamento expirado = agendamentoAgendadoComInicio(AGORA.plusMinutes(10));
        Agendamento dentroDoPrazo = agendamentoAgendadoComInicio(AGORA.plusHours(3));

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porStatus(StatusAgendamento.AGENDADO)).thenReturn(List.of(expirado, dentroDoPrazo));

        LiberarHorariosNaoConfirmadosService service = new LiberarHorariosNaoConfirmadosService(repository, RELOGIO);

        int liberados = service.liberarHorariosNaoConfirmados();

        assertThat(liberados).isEqualTo(1);
        verify(repository, times(1)).salvar(any());
        verify(repository).salvar(expirado);
    }
}