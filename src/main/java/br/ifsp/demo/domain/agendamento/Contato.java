package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.exception.RegraDeNegocioException;

public record Contato(String nome, String telefone) {
    private static final int TELEFONE_MINIMO_DE_DIGITOS = 10;
    private static final int TELEFONE_MAXIMO_DE_DIGITOS = 11;

    public Contato {
        if (telefone.length() < TELEFONE_MINIMO_DE_DIGITOS || telefone.length() > TELEFONE_MAXIMO_DE_DIGITOS) {
            throw new RegraDeNegocioException("O telefone deve ser válido");
        }
    }
}
