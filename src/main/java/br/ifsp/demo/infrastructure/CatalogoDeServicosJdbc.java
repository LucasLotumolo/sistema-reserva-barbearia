package br.ifsp.demo.infrastructure;

import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.servico.CatalogoDeServicos;
import br.ifsp.demo.domain.servico.Servico;
import br.ifsp.demo.domain.servico.ServicoId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CatalogoDeServicosJdbc implements CatalogoDeServicos {
    private final JdbcTemplate jdbc;

    public CatalogoDeServicosJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Servico> porId(ServicoId id) {
        return jdbc.query("SELECT * FROM servico WHERE id = ?",
                (rs, rowNum) -> new Servico(
                        new ServicoId(UUID.fromString(rs.getString("id"))),
                        rs.getString("nome"),
                        new Dinheiro(new BigDecimal(rs.getString("preco"))),
                        rs.getInt("duracao_em_minutos")),
                id.valor().toString()).stream().findFirst();
    }
}
