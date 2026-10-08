package br.ifsp.demo.domain.comum;

import br.ifsp.demo.exception.RegraDeNegocioException;

import java.time.LocalDateTime;

public record Periodo(LocalDateTime inicio, LocalDateTime fim) {
    public Periodo {
        if (!fim.isAfter(inicio)) {
            throw new RegraDeNegocioException("O fim do periodo deve ser posterior ao início");
        }
    }

    public boolean sobrepoe(Periodo outro) {
        return this.inicio().isBefore(outro.fim()) && outro.inicio().isBefore(this.fim());
    }
}