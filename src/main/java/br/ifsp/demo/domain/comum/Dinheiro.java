package br.ifsp.demo.domain.comum;

import java.math.BigDecimal;

public record Dinheiro(BigDecimal valor) {

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(this.valor().add(outro.valor()));
    }
}