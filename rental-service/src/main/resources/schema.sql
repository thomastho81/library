-- Tabela de aluguel: um registro por reserva, atualizado na devolução.
-- Status: 0=PENDING, 1=Reservado, 2=Devolvido, 3=Cancelado, 4=Reserva processada com erro (RESERVE_FAILED).
CREATE TABLE IF NOT EXISTS tb_aluguel (
    id              BIGSERIAL PRIMARY KEY,
    id_livro        BIGINT NOT NULL,
    quantidade      INTEGER NOT NULL,
    status          INTEGER NOT NULL DEFAULT 1,
    data_reserva    TIMESTAMP NOT NULL,
    data_devolucao  TIMESTAMP
);
