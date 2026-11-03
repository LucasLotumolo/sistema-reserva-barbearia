package br.ifsp.demo.controller;

import br.ifsp.demo.domain.agendamento.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponse(int nota, LocalDateTime registradaEm) {
    public static AvaliacaoResponse de(Avaliacao avaliacao) {
        if (avaliacao == null) {
            return null;
        }
        return new AvaliacaoResponse(avaliacao.nota(), avaliacao.registradaEm());
    }
}
