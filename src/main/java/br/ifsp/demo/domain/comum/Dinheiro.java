package br.ifsp.demo.domain.comum;

import java.math.BigDecimal;

public record Dinheiro(BigDecimal valor) {

    public static Dinheiro zero() {
        throw new UnsupportedOperationException("não implementado");
    }

    public Dinheiro somar(Dinheiro outro) {
        throw new UnsupportedOperationException("não implementado");
    }
}
