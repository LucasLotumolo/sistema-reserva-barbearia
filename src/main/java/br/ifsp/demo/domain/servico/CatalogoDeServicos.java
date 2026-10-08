package br.ifsp.demo.domain.servico;

import java.util.Optional;

public interface CatalogoDeServicos {
    Optional<Servico> porId(ServicoId id);
}
