package br.ifsp.demo.infrastructure;

import br.ifsp.demo.domain.agendamento.Agendamento;
import br.ifsp.demo.domain.agendamento.AgendamentoId;
import br.ifsp.demo.domain.agendamento.AgendamentoRepository;
import br.ifsp.demo.domain.agendamento.Avaliacao;
import br.ifsp.demo.domain.agendamento.Contato;
import br.ifsp.demo.domain.agendamento.ItemDeServico;
import br.ifsp.demo.domain.agendamento.ItemId;
import br.ifsp.demo.domain.agendamento.StatusAgendamento;
import br.ifsp.demo.domain.comum.BarbeiroId;
import br.ifsp.demo.domain.comum.ClienteId;
import br.ifsp.demo.domain.comum.Dinheiro;
import br.ifsp.demo.domain.comum.Periodo;
import br.ifsp.demo.domain.servico.ServicoId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgendamentoRepositoryJdbc implements AgendamentoRepository {
    private static final String SELECT_AGENDAMENTO = "SELECT * FROM agendamento";

    private final JdbcTemplate jdbc;

    public AgendamentoRepositoryJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void salvar(Agendamento agendamento) {
        Avaliacao avaliacao = agendamento.getAvaliacao();

        jdbc.update("""
                INSERT INTO agendamento (id, cliente_id, barbeiro_id, contato_nome, contato_telefone,
                                         inicio, fim, status, quantidade_de_reagendamentos,
                                         avaliacao_nota, avaliacao_registrada_em)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    contato_nome = excluded.contato_nome,
                    contato_telefone = excluded.contato_telefone,
                    inicio = excluded.inicio,
                    fim = excluded.fim,
                    status = excluded.status,
                    quantidade_de_reagendamentos = excluded.quantidade_de_reagendamentos,
                    avaliacao_nota = excluded.avaliacao_nota,
                    avaliacao_registrada_em = excluded.avaliacao_registrada_em
                """,
                agendamento.getId().valor().toString(),
                agendamento.getClienteId().valor().toString(),
                agendamento.getBarbeiroId().valor().toString(),
                agendamento.getContato().nome(),
                agendamento.getContato().telefone(),
                agendamento.getPeriodo().inicio().toString(),
                agendamento.getPeriodo().fim().toString(),
                agendamento.getStatus().name(),
                agendamento.getQuantidadeDeReagendamentos(),
                avaliacao == null ? null : avaliacao.nota(),
                avaliacao == null ? null : avaliacao.registradaEm().toString());

        substituirItens(agendamento);
    }

    @Override
    public Optional<Agendamento> porId(AgendamentoId id) {
        return jdbc.query(SELECT_AGENDAMENTO + " WHERE id = ?", mapper(), id.valor().toString())
                .stream().findFirst();
    }

    @Override
    public List<Agendamento> porCliente(ClienteId clienteId) {
        return jdbc.query(SELECT_AGENDAMENTO + " WHERE cliente_id = ? ORDER BY inicio",
                mapper(), clienteId.valor().toString());
    }

    @Override
    public List<Agendamento> porBarbeiroEData(BarbeiroId barbeiroId, LocalDate data) {
        return jdbc.query(SELECT_AGENDAMENTO + " WHERE barbeiro_id = ? AND substr(inicio, 1, 10) = ? ORDER BY inicio",
                mapper(), barbeiroId.valor().toString(), data.toString());
    }

    @Override
    public List<Agendamento> porStatus(StatusAgendamento status) {
        return jdbc.query(SELECT_AGENDAMENTO + " WHERE status = ? ORDER BY inicio",
                mapper(), status.name());
    }

    private void substituirItens(Agendamento agendamento) {
        String agendamentoId = agendamento.getId().valor().toString();
        jdbc.update("DELETE FROM item_de_servico WHERE agendamento_id = ?", agendamentoId);

        int ordem = 0;
        for (ItemDeServico item : agendamento.getItens()) {
            jdbc.update("""
                    INSERT INTO item_de_servico (id, agendamento_id, servico_id, nome_do_servico,
                                                 preco, duracao_em_minutos, ordem)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    item.getId().valor().toString(),
                    agendamentoId,
                    item.getServicoId().valor().toString(),
                    item.getNomeDoServico(),
                    item.getPreco().valor().toPlainString(),
                    item.getDuracaoEmMinutos(),
                    ordem++);
        }
    }

    private RowMapper<Agendamento> mapper() {
        return (rs, rowNum) -> {
            AgendamentoId id = new AgendamentoId(UUID.fromString(rs.getString("id")));
            return Agendamento.reconstituir(
                    id,
                    new ClienteId(UUID.fromString(rs.getString("cliente_id"))),
                    new BarbeiroId(UUID.fromString(rs.getString("barbeiro_id"))),
                    new Contato(rs.getString("contato_nome"), rs.getString("contato_telefone")),
                    new Periodo(LocalDateTime.parse(rs.getString("inicio")),
                            LocalDateTime.parse(rs.getString("fim"))),
                    itensDo(id),
                    StatusAgendamento.valueOf(rs.getString("status")),
                    rs.getInt("quantidade_de_reagendamentos"),
                    avaliacaoDe(rs));
        };
    }

    private List<ItemDeServico> itensDo(AgendamentoId id) {
        return jdbc.query("SELECT * FROM item_de_servico WHERE agendamento_id = ? ORDER BY ordem",
                (rs, rowNum) -> new ItemDeServico(
                        new ItemId(UUID.fromString(rs.getString("id"))),
                        new ServicoId(UUID.fromString(rs.getString("servico_id"))),
                        rs.getString("nome_do_servico"),
                        new Dinheiro(new BigDecimal(rs.getString("preco"))),
                        rs.getInt("duracao_em_minutos")),
                id.valor().toString());
    }

    private Avaliacao avaliacaoDe(ResultSet rs) throws SQLException {
        int nota = rs.getInt("avaliacao_nota");
        if (rs.wasNull()) {
            return null;
        }
        return new Avaliacao(nota, LocalDateTime.parse(rs.getString("avaliacao_registrada_em")));
    }
}
