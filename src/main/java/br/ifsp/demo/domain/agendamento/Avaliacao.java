package br.ifsp.demo.domain.agendamento;

import java.time.LocalDateTime;

public record Avaliacao(int nota, LocalDateTime registradaEm) {
}