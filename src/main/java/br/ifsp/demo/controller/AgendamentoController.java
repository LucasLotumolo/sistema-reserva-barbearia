package br.ifsp.demo.controller;

import br.ifsp.demo.application.AgendarAtendimentoService;
import br.ifsp.demo.application.AlterarServicosDoAgendamentoService;
import br.ifsp.demo.application.AvaliarAtendimentoService;
import br.ifsp.demo.application.ConfirmarPresencaService;
import br.ifsp.demo.application.ConsultarAgendamentosService;
import br.ifsp.demo.application.ReagendarAtendimentoService;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.exception.AgendamentoNaoEncontradoException;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/agendamentos")
@Tag(name = "Agendamentos API")
public class AgendamentoController {

    private final AuthenticationInfoService authService;
    private final AgendarAtendimentoService agendarAtendimentoService;
    private final ConsultarAgendamentosService consultarAgendamentosService;
    private final ReagendarAtendimentoService reagendarAtendimentoService;
    private final ConfirmarPresencaService confirmarPresencaService;
    private final AvaliarAtendimentoService avaliarAtendimentoService;
    private final AlterarServicosDoAgendamentoService alterarServicosDoAgendamentoService;

    public AgendamentoController(AuthenticationInfoService authService,
                                 AgendarAtendimentoService agendarAtendimentoService,
                                 ConsultarAgendamentosService consultarAgendamentosService,
                                 ReagendarAtendimentoService reagendarAtendimentoService,
                                 ConfirmarPresencaService confirmarPresencaService,
                                 AvaliarAtendimentoService avaliarAtendimentoService,
                                 AlterarServicosDoAgendamentoService alterarServicosDoAgendamentoService) {
        this.authService = authService;
        this.agendarAtendimentoService = agendarAtendimentoService;
        this.consultarAgendamentosService = consultarAgendamentosService;
        this.reagendarAtendimentoService = reagendarAtendimentoService;
        this.confirmarPresencaService = confirmarPresencaService;
        this.avaliarAtendimentoService = avaliarAtendimentoService;
        this.alterarServicosDoAgendamentoService = alterarServicosDoAgendamentoService;
    }

    @Operation(summary = "Agenda um atendimento para o cliente autenticado.")
    @PostMapping
    public ResponseEntity<AgendamentoResponse> agendar(@RequestBody AgendarAtendimentoRequest request) {
        List<ServicoId> servicos = request.servicos().stream().map(ServicoId::new).toList();
        Agendamento agendamento = agendarAtendimentoService.agendar(
                clienteAutenticado(),
                new BarbeiroId(request.barbeiroId()),
                new Contato(request.nomeDoContato(), request.telefoneDoContato()),
                request.inicio(),
                servicos);
        return new ResponseEntity<>(AgendamentoResponse.de(agendamento), HttpStatus.CREATED);
    }

    @Operation(summary = "Lista os agendamentos do cliente autenticado, ordenados pelo início.",
            description = "Com futuros=true, retorna apenas os agendamentos ativos.")
    @GetMapping
    public ResponseEntity<List<AgendamentoResponse>> listar(
            @RequestParam(defaultValue = "false") boolean futuros) {
        List<Agendamento> agendamentos = futuros
                ? consultarAgendamentosService.listarFuturosDoCliente(clienteAutenticado())
                : consultarAgendamentosService.listarDoCliente(clienteAutenticado());
        return ResponseEntity.ok(agendamentos.stream().map(AgendamentoResponse::de).toList());
    }

    @Operation(summary = "Detalha um agendamento do cliente autenticado.")
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(AgendamentoResponse.de(buscarDoClienteAutenticado(id)));
    }

    @Operation(summary = "Reagenda um atendimento do cliente autenticado para um novo início.")
    @PostMapping("/{id}/reagendamento")
    public ResponseEntity<AgendamentoResponse> reagendar(@PathVariable UUID id,
                                                         @RequestBody ReagendarAtendimentoRequest request) {
        Agendamento agendamento = buscarDoClienteAutenticado(id);
        Agendamento reagendado = reagendarAtendimentoService.reagendar(agendamento.getId(), request.novoInicio());
        return ResponseEntity.ok(AgendamentoResponse.de(reagendado));
    }

    @Operation(summary = "Confirma a presença do cliente autenticado no atendimento.")
    @PostMapping("/{id}/confirmacao")
    public ResponseEntity<Void> confirmarPresenca(@PathVariable UUID id) {
        Agendamento agendamento = buscarDoClienteAutenticado(id);
        confirmarPresencaService.confirmar(agendamento.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Registra a avaliação do cliente autenticado para um atendimento realizado.")
    @PostMapping("/{id}/avaliacao")
    public ResponseEntity<Void> avaliar(@PathVariable UUID id, @RequestBody AvaliarAtendimentoRequest request) {
        Agendamento agendamento = buscarDoClienteAutenticado(id);
        avaliarAtendimentoService.avaliar(agendamento.getId(), request.nota());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Inclui um serviço do catálogo no agendamento do cliente autenticado.")
    @PostMapping("/{id}/itens")
    public ResponseEntity<AgendamentoResponse> adicionarServico(@PathVariable UUID id,
                                                                @RequestBody AdicionarServicoRequest request) {
        Agendamento agendamento = buscarDoClienteAutenticado(id);
        alterarServicosDoAgendamentoService.adicionarServico(agendamento.getId(), new ServicoId(request.servicoId()));
        return ResponseEntity.ok(AgendamentoResponse.de(buscarDoClienteAutenticado(id)));
    }

    @Operation(summary = "Remove um serviço do agendamento do cliente autenticado.")
    @DeleteMapping("/{id}/itens/{itemId}")
    public ResponseEntity<AgendamentoResponse> removerServico(@PathVariable UUID id, @PathVariable UUID itemId) {
        Agendamento agendamento = buscarDoClienteAutenticado(id);
        alterarServicosDoAgendamentoService.removerServico(agendamento.getId(), new ItemId(itemId));
        return ResponseEntity.ok(AgendamentoResponse.de(buscarDoClienteAutenticado(id)));
    }

    // Os serviços buscam só pelo id; sem esta checagem um cliente poderia ler ou alterar a reserva de outro.
    private Agendamento buscarDoClienteAutenticado(UUID id) {
        Agendamento agendamento = consultarAgendamentosService.buscarPorId(new AgendamentoId(id));
        if (!agendamento.getClienteId().equals(clienteAutenticado())) {
            throw new AgendamentoNaoEncontradoException("Agendamento não encontrado");
        }
        return agendamento;
    }

    private ClienteId clienteAutenticado() {
        return new ClienteId(authService.getAuthenticatedUserId());
    }
}
