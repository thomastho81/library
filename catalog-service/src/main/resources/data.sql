-- Cria a tabela tb_livro se não existir.
CREATE TABLE IF NOT EXISTS tb_livro (
    id                BIGSERIAL PRIMARY KEY,
    titulo            VARCHAR(255) NOT NULL,
    autor             VARCHAR(255) NOT NULL,
    categoria         VARCHAR(255),
    genero            VARCHAR(255),
    descricao         TEXT,
    isbn              VARCHAR(50),
    ano_publicacao    INTEGER,
    ativo             BOOLEAN NOT NULL DEFAULT true,
    data_criacao      TIMESTAMP,
    data_atualizacao  TIMESTAMP
);
