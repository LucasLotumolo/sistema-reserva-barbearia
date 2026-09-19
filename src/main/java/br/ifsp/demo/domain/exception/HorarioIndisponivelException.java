package br.ifsp.demo.domain.exception;

public class HorarioIndisponivelException extends RegraDeNegocioException {
    public HorarioIndisponivelException(String message) {
        super(message);
    }
}
