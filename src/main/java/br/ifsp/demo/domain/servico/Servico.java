package br.ifsp.demo.domain.servico;

import br.ifsp.demo.domain.comum.Dinheiro;

public record Servico(ServicoId id, String nome, Dinheiro preco, int duracaoEmMinutos) {
}
