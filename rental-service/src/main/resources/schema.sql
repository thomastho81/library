-- Usuários do sistema (quem pode alugar). id como BIGSERIAL (Long).
-- perfil: 0=USER (usuário), 1=GESTOR (gestor).
CREATE TABLE IF NOT EXISTS tb_usuarios (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(255) NOT NULL,
    idade           INTEGER NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    ativo           BOOLEAN NOT NULL DEFAULT true,
    perfil          INTEGER NOT NULL DEFAULT 0,
    data_cadastro   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Migração: adiciona coluna perfil se não existir (bancos já criados antes).
ALTER TABLE tb_usuarios ADD COLUMN IF NOT EXISTS perfil INTEGER NOT NULL DEFAULT 0;

-- Tabela de aluguel: um registro por reserva, atualizado na devolução.
-- Status: 0=PENDING, 1=RESERVED, 2=RETURNED, 3=CANCELLED, 4=RESERVE_FAILED, 5=RETURN_REQUESTED.
-- id_usuario referencia tb_usuarios(id).
CREATE TABLE IF NOT EXISTS tb_aluguel (
    id              BIGSERIAL PRIMARY KEY,
    id_usuario      BIGINT NOT NULL REFERENCES tb_usuarios(id),
    id_livro        BIGINT NOT NULL,
    quantidade      INTEGER NOT NULL,
    status          INTEGER NOT NULL DEFAULT 0,
    data_reserva    TIMESTAMP NOT NULL,
    data_devolucao  TIMESTAMP
);
