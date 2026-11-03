package br.ifsp.demo.controller;

import br.ifsp.demo.application.ConsultarAgendamentosService;
import br.ifsp.demo.application.ConsultarHorariosDisponiveisService;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.servico.ServicoId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/barbeiros/{barbeiroId}")
@Tag(name = "Agenda do Barbeiro API")
public class AgendaDoBarbeiroController {

    private final ConsultarAgendamentosService consultarAgendamentosService;
    private final ConsultarHorariosDisponiveisService consultarHorariosDisponiveisService;

    public AgendaDoBarbeiroController(ConsultarAgendamentosService consultarAgendamentosService,
                                      ConsultarHorariosDisponiveisService consultarHorariosDisponiveisService) {
        this.consultarAgendamentosService = consultarAgendamentosService;
        this.consultarHorariosDisponiveisService = consultarHorariosDisponiveisService;
    }

    @Operation(summary = "Lista os períodos ocupados do barbeiro na data informada.")
    @GetMapping("/horarios-ocupados")
    public ResponseEntity<List<PeriodoResponse>> horariosOcupados(
            @PathVariable UUID barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<PeriodoResponse> ocupados = consultarAgendamentosService
                .horariosOcupados(new BarbeiroId(barbeiroId), data).stream()
                .map(PeriodoResponse::de)
                .toList();
        return ResponseEntity.ok(ocupados);
    }

    @Operation(summary = "Lista os intervalos livres do barbeiro na data que comportam os serviços informados.")
    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<List<PeriodoResponse>> horariosDisponiveis(
            @PathVariable UUID barbeiroId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam List<UUID> servicos) {
        List<ServicoId> servicoIds = servicos.stream().map(ServicoId::new).toList();
        List<PeriodoResponse> disponiveis = consultarHorariosDisponiveisService
                .consultar(new BarbeiroId(barbeiroId), data, servicoIds).stream()
                .map(PeriodoResponse::de)
                .toList();
        return ResponseEntity.ok(disponiveis);
    }
}
