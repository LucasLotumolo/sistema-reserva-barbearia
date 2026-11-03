package br.ifsp.demo.controller;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        UUID barbeiroId,
        String nomeDoContato,
        String telefoneDoContato,
        LocalDateTime inicio,
        LocalDateTime fim,
        StatusAgendamento status,
        BigDecimal valorTotal,
        int duracaoTotalEmMinutos,
        int quantidadeDeReagendamentos,
        List<ItemDeServicoResponse> itens,
        AvaliacaoResponse avaliacao
) {
    public static AgendamentoResponse de(Agendamento agendamento) {
        return new AgendamentoResponse(
                agendamento.getId().valor(),
                agendamento.getBarbeiroId().valor(),
                agendamento.getContato().nome(),
                agendamento.getContato().telefone(),
                agendamento.getPeriodo().inicio(),
                agendamento.getPeriodo().fim(),
                agendamento.getStatus(),
                agendamento.valorTotal().valor(),
                agendamento.duracaoTotalEmMinutos(),
                agendamento.getQuantidadeDeReagendamentos(),
                agendamento.getItens().stream().map(ItemDeServicoResponse::de).toList(),
                AvaliacaoResponse.de(agendamento.getAvaliacao()));
    }
}
