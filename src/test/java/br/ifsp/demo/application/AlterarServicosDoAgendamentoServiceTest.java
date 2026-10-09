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
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import br.ifsp.demo.exception.ServicoNaoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("US-03 — Serviço de aplicação para alterar serviços do agendamento")
class AlterarServicosDoAgendamentoServiceTest {

    private Agendamento agendamentoComUmServico(AgendamentoId id, BarbeiroId barbeiroId, LocalDateTime inicio) {
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
                new Contato("Maria Silva", "11987654321"),
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
    @DisplayName("[OK] Adicionar serviço com sucesso salva o agendamento")
    void adicionarServicoComSucessoDeveSalvarOAgendamento() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        Agendamento agendamento = agendamentoComUmServico(id, barbeiroId, inicio);

        ServicoId barbaId = new ServicoId(UUID.randomUUID());
        Servico barba = new Servico(barbaId, "Barba", new Dinheiro(new BigDecimal("25.00")), 20);

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));
        when(repository.porBarbeiroEData(barbeiroId, inicio.toLocalDate())).thenReturn(List.of());

        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);
        when(catalogo.porId(barbaId)).thenReturn(Optional.of(barba));

        AlterarServicosDoAgendamentoService service =
                new AlterarServicosDoAgendamentoService(repository, catalogo);

        service.adicionarServico(id, barbaId);

        assertThat(agendamento.duracaoTotalEmMinutos()).isEqualTo(50);
        verify(repository).salvar(agendamento);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Adicionar serviço em agendamento inexistente é rejeitado")
    void adicionarServicoEmAgendamentoInexistenteDeveSerRejeitado() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        ServicoId servicoId = new ServicoId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.empty());
        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);

        AlterarServicosDoAgendamentoService service =
                new AlterarServicosDoAgendamentoService(repository, catalogo);

        assertThatThrownBy(() -> service.adicionarServico(id, servicoId))
                .isInstanceOf(AgendamentoNaoEncontradoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Adicionar serviço inexistente no catálogo é rejeitado")
    void adicionarServicoInexistenteNoCatalogoDeveSerRejeitado() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        BarbeiroId barbeiroId = new BarbeiroId(UUID.randomUUID());
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);
        Agendamento agendamento = agendamentoComUmServico(id, barbeiroId, inicio);
        ServicoId servicoInexistente = new ServicoId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));
        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);
        when(catalogo.porId(servicoInexistente)).thenReturn(Optional.empty());

        AlterarServicosDoAgendamentoService service =
                new AlterarServicosDoAgendamentoService(repository, catalogo);

        assertThatThrownBy(() -> service.adicionarServico(id, servicoInexistente))
                .isInstanceOf(ServicoNaoEncontradoException.class);

        verify(repository, never()).salvar(any());
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[OK] Remover serviço com sucesso salva o agendamento")
    void removerServicoComSucessoDeveSalvarOAgendamento() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 22, 10, 0);

        ItemDeServico corte = new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                "Corte", new Dinheiro(new BigDecimal("40.00")), 30);
        ItemDeServico barba = new ItemDeServico(new ItemId(UUID.randomUUID()), new ServicoId(UUID.randomUUID()),
                "Barba", new Dinheiro(new BigDecimal("25.00")), 20);

        Agendamento agendamento = Agendamento.reconstituir(
                id, new ClienteId(UUID.randomUUID()), new BarbeiroId(UUID.randomUUID()),
                new Contato("Maria Silva", "11987654321"), new Periodo(inicio, inicio.plusMinutes(50)),
                List.of(corte, barba), StatusAgendamento.AGENDADO, 0, null);

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.of(agendamento));
        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);

        AlterarServicosDoAgendamentoService service =
                new AlterarServicosDoAgendamentoService(repository, catalogo);

        service.removerServico(id, barba.getId());

        assertThat(agendamento.getItens()).hasSize(1);
        verify(repository).salvar(agendamento);
    }

    @Test
    @Tag("UnitTest")
    @Tag("TDD")
    @DisplayName("[ERROR] Remover serviço de agendamento inexistente é rejeitado")
    void removerServicoDeAgendamentoInexistenteDeveSerRejeitado() {
        AgendamentoId id = new AgendamentoId(UUID.randomUUID());
        ItemId itemId = new ItemId(UUID.randomUUID());

        AgendamentoRepository repository = mock(AgendamentoRepository.class);
        when(repository.porId(id)).thenReturn(Optional.empty());
        CatalogoDeServicos catalogo = mock(CatalogoDeServicos.class);

        AlterarServicosDoAgendamentoService service =
                new AlterarServicosDoAgendamentoService(repository, catalogo);

        assertThatThrownBy(() -> service.removerServico(id, itemId))
                .isInstanceOf(AgendamentoNaoEncontradoException.class);

        verify(repository, never()).salvar(any());
    }
}