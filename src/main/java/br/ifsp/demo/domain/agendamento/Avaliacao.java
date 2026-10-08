package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.exception.RegraDeNegocioException;

import java.time.LocalDateTime;

public record Avaliacao(int nota, LocalDateTime registradaEm) {
    private static final int NOTA_MINIMA = 1;
    private static final int NOTA_MAXIMA = 5;

    public Avaliacao {
        if (registradaEm == null) {
            throw new RegraDeNegocioException("O instante da avaliação deve ser informado");
        }
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new RegraDeNegocioException("A nota deve estar entre 1 e 5");
        }
    }
}
