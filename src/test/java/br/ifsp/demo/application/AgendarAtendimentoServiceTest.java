package br.ifsp.demo.application;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Serviço de aplicação — Agendar atendimento")
class AgendarAtendimentoServiceTest {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("S1.1 - [OK] Agenda o atendimento e salva no repositório")
    void deveAgendarESalvarOAtendimento() {
        LocalDateTime agora = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 2, 14, 0);
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        ServicoId corteId = new ServicoId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);
        when(catalogo.porId(corteId)).thenReturn(Optional.of(
                new Servico(corteId, "Corte", new Dinheiro(new BigDecimal("40.00")), 30)));
        when(repository.porBarbeiroEData(barbeiroId, inicio.toLocalDate())).thenReturn(List.of());
        Clock clock = Clock.fixed(agora.atZone(FUSO).toInstant(), FUSO);
        AgendarAtendimentoService service = new AgendarAtendimentoService(repository, catalogo, clock);

        Agendamento agendamento = service.agendar(
                new ClienteId(UUID.randomUUID()),
                barbeiroId,
                new Contato("Lucas", "16999999999"),
                inicio,
                List.of(corteId)
        );

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(agendamento.getPeriodo().inicio()).isEqualTo(inicio);
        assertThat(agendamento.getPeriodo().fim()).isEqualTo(inicio.plusMinutes(30));
        assertThat(agendamento.valorTotal()).isEqualTo(new Dinheiro(new BigDecimal("40.00")));
        verify(repository).salvar(agendamento);
    }
}
