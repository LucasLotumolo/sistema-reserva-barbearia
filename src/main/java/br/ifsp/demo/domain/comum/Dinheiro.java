package br.ifsp.demo.domain.comum;

import br.ifsp.demo.exception.RegraDeNegocioException;

import java.math.BigDecimal;

public record Dinheiro(BigDecimal valor) {
    public Dinheiro {
        if (valor.signum() < 0) {
            throw new RegraDeNegocioException("O valor não pode ser negativo");
        }
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(this.valor().add(outro.valor()));
    }
}
