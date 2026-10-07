package br.ifsp.demo.domain.agendamento;

import br.ifsp.demo.exception.RegraDeNegocioException;

import java.util.regex.Pattern;

public record Contato(String nome, String telefone) {
    private static final Pattern TELEFONE_VALIDO = Pattern.compile("\\d{10,11}");

    public Contato {
        if (!TELEFONE_VALIDO.matcher(telefone).matches()) {
            throw new RegraDeNegocioException("O telefone deve ser válido");
        }
    }
}
