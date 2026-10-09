package br.ifsp.demo.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgendarAtendimentoRequest(
        @Schema(description = "Barbeiro que fará o atendimento")
        UUID barbeiroId,
        @Schema(description = "Nome do cliente para contato", example = "João Silva")
        String nomeDoContato,
        @Schema(description = "Telefone com DDD, apenas dígitos", example = "16999998888")
        String telefoneDoContato,
        @Schema(description = "Início do atendimento", example = "2026-10-20T10:00:00")
        LocalDateTime inicio,
        @Schema(description = "Serviços do catálogo, na ordem em que serão realizados")
        List<UUID> servicos
) {}
