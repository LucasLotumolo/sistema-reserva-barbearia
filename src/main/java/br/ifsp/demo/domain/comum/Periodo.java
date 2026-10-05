package br.ifsp.demo.domain.comum;

import java.time.Duration;
import java.time.LocalDateTime;

public record Periodo(LocalDateTime inicio, LocalDateTime fim) {
    public int duracaoEmMinutos() {
        return (int) Duration.between(inicio, fim).toMinutes();
    }

    public boolean sobrepoe(Periodo outro) {
        return this.inicio().isBefore(outro.fim()) && outro.inicio().isBefore(this.fim());
    }
}