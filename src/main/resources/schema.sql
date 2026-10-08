CREATE TABLE IF NOT EXISTS agendamento (
    id                          TEXT    PRIMARY KEY,
    cliente_id                  TEXT    NOT NULL,
    barbeiro_id                 TEXT    NOT NULL,
    contato_nome                TEXT    NOT NULL,
    contato_telefone            TEXT    NOT NULL,
    inicio                      TEXT    NOT NULL,
    fim                         TEXT    NOT NULL,
    status                      TEXT    NOT NULL,
    quantidade_de_reagendamentos INTEGER NOT NULL DEFAULT 0,
    avaliacao_nota              INTEGER,
    avaliacao_registrada_em     TEXT
);

CREATE TABLE IF NOT EXISTS item_de_servico (
    id                TEXT    PRIMARY KEY,
    agendamento_id    TEXT    NOT NULL REFERENCES agendamento (id) ON DELETE CASCADE,
    servico_id        TEXT    NOT NULL,
    nome_do_servico   TEXT    NOT NULL,
    preco             TEXT    NOT NULL,
    duracao_em_minutos INTEGER NOT NULL,
    ordem             INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_agendamento_cliente ON agendamento (cliente_id);
CREATE INDEX IF NOT EXISTS idx_agendamento_barbeiro_inicio ON agendamento (barbeiro_id, inicio);
CREATE INDEX IF NOT EXISTS idx_agendamento_status ON agendamento (status);
CREATE INDEX IF NOT EXISTS idx_item_agendamento ON item_de_servico (agendamento_id);
