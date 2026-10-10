package br.ifsp.demo.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AdicionarServicoRequest(
        @Schema(description = "Serviço do catálogo a incluir no agendamento")
        UUID servicoId
) {}
