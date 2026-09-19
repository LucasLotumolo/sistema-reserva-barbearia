package br.ifsp.demo.domain.comum;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioDeFuncionamento(LocalTime abertura, LocalTime fechamento) {

    public static final HorarioDeFuncionamento PADRAO =
            new HorarioDeFuncionamento(LocalTime.of(9, 0), LocalTime.of(19, 0));

    public boolean contem(Periodo periodo) {
        throw new UnsupportedOperationException("não implementado");
    }

    public Periodo expedienteDe(LocalDate data) {
        throw new UnsupportedOperationException("não implementado");
    }
}
