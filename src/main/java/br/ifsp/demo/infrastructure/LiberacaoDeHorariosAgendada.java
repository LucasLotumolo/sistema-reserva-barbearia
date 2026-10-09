package br.ifsp.demo.infrastructure;

import br.ifsp.demo.application.LiberarHorariosNaoConfirmadosService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LiberacaoDeHorariosAgendada {
    private final LiberarHorariosNaoConfirmadosService service;

    public LiberacaoDeHorariosAgendada(LiberarHorariosNaoConfirmadosService service) {
        this.service = service;
    }

    // A janela de confirmação fecha 30 minutos antes do início; rodar a cada minuto limita o atraso da liberação.
    @Scheduled(fixedDelay = 1, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void liberar() {
        service.liberarHorariosNaoConfirmados();
    }
}
