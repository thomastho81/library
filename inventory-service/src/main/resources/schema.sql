-- Cria a tabela tb_inventario se não existir.
CREATE TABLE IF NOT EXISTS tb_inventario (
    id                  BIGSERIAL PRIMARY KEY,
    id_livro            BIGINT NOT NULL,
    total_copias        INTEGER NOT NULL DEFAULT 0,
    copias_disponiveis  INTEGER NOT NULL DEFAULT 0,
    copias_reservadas   INTEGER NOT NULL DEFAULT 0,
    ativo               BOOLEAN NOT NULL DEFAULT true,
    versao              BIGINT NOT NULL DEFAULT 0,
    data_criacao        TIMESTAMP,
    data_atualizacao    TIMESTAMP,
    CONSTRAINT uk_inventario_id_livro UNIQUE (id_livro)
);
