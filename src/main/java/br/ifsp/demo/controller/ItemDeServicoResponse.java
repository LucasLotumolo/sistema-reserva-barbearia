package br.ifsp.demo.controller;

import br.ifsp.demo.domain.agendamento.ItemDeServico;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemDeServicoResponse(
        UUID id,
        UUID servicoId,
        String nomeDoServico,
        BigDecimal preco,
        int duracaoEmMinutos
) {
    public static ItemDeServicoResponse de(ItemDeServico item) {
        return new ItemDeServicoResponse(item.getId().valor(), item.getServicoId().valor(),
                item.getNomeDoServico(), item.getPreco().valor(), item.getDuracaoEmMinutos());
    }
}
