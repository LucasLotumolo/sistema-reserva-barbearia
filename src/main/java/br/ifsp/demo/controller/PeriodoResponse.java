package br.ifsp.demo.controller;

import br.ifsp.demo.domain.comum.Periodo;

import java.time.LocalDateTime;

public record PeriodoResponse(LocalDateTime inicio, LocalDateTime fim) {
    public static PeriodoResponse de(Periodo periodo) {
        return new PeriodoResponse(periodo.inicio(), periodo.fim());
    }
}
