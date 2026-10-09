package br.ifsp.demo.controller;

import io.swagger.v3.oas.annotations.media.Schema;

public record AvaliarAtendimentoRequest(
        @Schema(description = "Nota de 1 a 5", example = "5")
        int nota
) {}
