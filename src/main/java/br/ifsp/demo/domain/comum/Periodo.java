package br.ifsp.demo.domain.comum;

import java.time.LocalDateTime;

public record Periodo(LocalDateTime inicio, LocalDateTime fim) {
    public boolean sobrepoe(Periodo outro) {
        return this.inicio().isBefore(outro.fim()) && outro.inicio().isBefore(this.fim());
    }
}