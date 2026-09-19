package br.ifsp.demo.domain.comum;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Periodo(LocalDateTime inicio, LocalDateTime fim) {

    public static Periodo comDuracao(LocalDateTime inicio, int minutos) {
        throw new UnsupportedOperationException("não implementado");
    }

    public int duracaoEmMinutos() {
        throw new UnsupportedOperationException("não implementado");
    }

    public boolean sobrepoe(Periodo outro) {
        throw new UnsupportedOperationException("não implementado");
    }

    public LocalDate data() {
        throw new UnsupportedOperationException("não implementado");
    }
}
