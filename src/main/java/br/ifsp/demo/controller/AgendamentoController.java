package br.ifsp.demo.controller;

import br.ifsp.demo.application.AgendarAtendimentoService;
import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.servico.ServicoId;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/agendamentos")
@Tag(name = "Agendamentos API")
public class AgendamentoController {

    private final AuthenticationInfoService authService;
    private final AgendarAtendimentoService agendarAtendimentoService;

    public AgendamentoController(AuthenticationInfoService authService,
                                 AgendarAtendimentoService agendarAtendimentoService) {
        this.authService = authService;
        this.agendarAtendimentoService = agendarAtendimentoService;
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

    private ClienteId clienteAutenticado() {
        return new ClienteId(authService.getAuthenticatedUserId());
    }
}
