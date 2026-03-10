-- Usuários do sistema (quem pode alugar). id como BIGSERIAL (Long).
CREATE TABLE IF NOT EXISTS tb_usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    idade           INTEGER NOT NULL,
    email           VARCHAR(255) NOT NULL,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Garante pelo menos um usuário para migração de dados existentes (id=1).
INSERT INTO tb_usuarios (id, nome, idade, email, ativo, data_cadastro)
SELECT 1, 'Sistema', 0, 'sistema@biblioteca.local', true, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tb_usuarios WHERE id = 1);

-- Tabela de aluguel: um registro por reserva, atualizado na devolução.
-- Status: 0=PENDING, 1=RESERVED, 2=RETURNED, 3=CANCELLED, 4=RESERVE_FAILED, 5=RETURNING.
CREATE TABLE IF NOT EXISTS tb_aluguel (
    id              BIGSERIAL PRIMARY KEY,
    id_usuario      BIGINT NOT NULL REFERENCES tb_usuarios(id),
    id_livro        BIGINT NOT NULL,
    quantidade      INTEGER NOT NULL,
    status          INTEGER NOT NULL DEFAULT 0,
    data_reserva    TIMESTAMP NOT NULL,
    data_devolucao  TIMESTAMP
);

-- Migração: se tb_aluguel já existir sem id_usuario, adiciona coluna e preenche com usuário 1.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'tb_aluguel')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'tb_aluguel' AND column_name = 'id_usuario') THEN
        ALTER TABLE tb_aluguel ADD COLUMN id_usuario BIGINT;
        UPDATE tb_aluguel SET id_usuario = 1 WHERE id_usuario IS NULL;
        ALTER TABLE tb_aluguel ALTER COLUMN id_usuario SET NOT NULL;
        ALTER TABLE tb_aluguel ADD CONSTRAINT fk_aluguel_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id);
    END IF;
END $$;
