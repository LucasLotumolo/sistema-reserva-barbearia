package br.ifsp.demo.domain.exception;

public class AgendamentoNaoEncontradoException extends RuntimeException {
    public AgendamentoNaoEncontradoException(String message) {
        super(message);
    }
}
