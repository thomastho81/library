-- Usuário de sistema (id 1) e gestor (id 2) para uso na aplicação.
-- perfil: 0=USER, 1=GESTOR.
INSERT INTO tb_usuarios (nome, idade, email, ativo, perfil, data_cadastro)
SELECT 'Usuário Sistema', 0, 'sistema@biblioteca.local', true, 0, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tb_usuarios WHERE email = 'sistema@biblioteca.local');

INSERT INTO tb_usuarios (nome, idade, email, ativo, perfil, data_cadastro)
SELECT 'Gestor Sistema', 0, 'gestor@biblioteca.local', true, 1, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM tb_usuarios WHERE email = 'gestor@biblioteca.local');
