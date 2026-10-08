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

CREATE TABLE IF NOT EXISTS servico (
    id                 TEXT    PRIMARY KEY,
    nome               TEXT    NOT NULL,
    preco              TEXT    NOT NULL,
    duracao_em_minutos INTEGER NOT NULL
);

INSERT OR IGNORE INTO servico (id, nome, preco, duracao_em_minutos) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Corte', '40.00', 30),
    ('22222222-2222-2222-2222-222222222222', 'Barba', '25.00', 20),
    ('33333333-3333-3333-3333-333333333333', 'Sobrancelha', '15.00', 15);
