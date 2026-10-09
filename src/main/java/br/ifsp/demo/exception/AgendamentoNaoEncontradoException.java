package br.ifsp.demo.exception;

public class AgendamentoNaoEncontradoException extends RuntimeException {
    public AgendamentoNaoEncontradoException(String message) {
        super(message);
    }
}
