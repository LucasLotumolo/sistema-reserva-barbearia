package br.ifsp.demo.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReagendarAtendimentoRequest(
        @Schema(description = "Novo início do atendimento", example = "2026-10-21T14:00:00")
        LocalDateTime novoInicio
) {}
