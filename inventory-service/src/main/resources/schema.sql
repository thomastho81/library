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

-- Eventos recebidos (idempotência). Operação: 1=Reserva, 2=Devolução. Status: 1=Processado, 2=Rejeitado.
-- id_usuario: usuário que solicitou o aluguel/devolução (propagado pelo rental-service).
CREATE TABLE IF NOT EXISTS tb_evento_processado (
    id_evento       VARCHAR(36) PRIMARY KEY,
    id_aluguel      BIGINT NOT NULL,
    id_usuario      BIGINT NOT NULL,
    id_livro        BIGINT NOT NULL,
    operacao        INTEGER NOT NULL,
    quantidade      INTEGER NOT NULL,
    status          INTEGER NOT NULL DEFAULT 1,
    processado_em   TIMESTAMP NOT NULL
);

-- Migração: adiciona id_usuario se a tabela já existir sem a coluna (preenche 0 para eventos antigos).
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tb_evento_processado')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tb_evento_processado' AND column_name = 'id_usuario') THEN
        ALTER TABLE tb_evento_processado ADD COLUMN id_usuario BIGINT;
        UPDATE tb_evento_processado SET id_usuario = 0 WHERE id_usuario IS NULL;
        ALTER TABLE tb_evento_processado ALTER COLUMN id_usuario SET NOT NULL;
    END IF;
END $$;
