package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.exception.RegraDeNegocioException;

import java.time.LocalDateTime;

public record Avaliacao(int nota, LocalDateTime registradaEm) {
    private static final int NOTA_MINIMA = 1;

    public Avaliacao {
        if (nota < NOTA_MINIMA) {
            throw new RegraDeNegocioException("A nota deve estar entre 1 e 5");
        }
    }
}
