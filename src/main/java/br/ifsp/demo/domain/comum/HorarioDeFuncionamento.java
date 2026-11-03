package br.ifsp.demo.domain.comum;

import java.time.LocalDate;
import java.time.LocalTime;

public record HorarioDeFuncionamento(LocalTime abertura, LocalTime fechamento) {

    public static final HorarioDeFuncionamento PADRAO =
            new HorarioDeFuncionamento(LocalTime.of(9, 0), LocalTime.of(19, 0));

    public Periodo expedienteDe(LocalDate data) {
        return new Periodo(data.atTime(abertura), data.atTime(fechamento));
    }
}
