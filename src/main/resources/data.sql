INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Admin', 'admin@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u', '(55) 95463-5154',
        '000.000.000-00', 'ROLE_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Cliente', 'cliente@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u',
        '(55) 94321-9876', '100.023.040-00', 'ROLE_CLIENTE', CURRENT_TIMESTAMP);
INSERT INTO clientes (id_usuario, status_aprovacao)
VALUES (2, 'APROVADO');

INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Prestador', 'prestador@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u',
        '(55) 91234-5678', '010.203.040-00', 'ROLE_PRESTADOR', CURRENT_TIMESTAMP);
INSERT INTO prestadores (id_usuario, status_aprovacao, recomendacoes)
VALUES (3, 'APROVADO', 0);

-- Dados de exemplo para Bairros (Santa Maria - RS)
INSERT INTO bairros (nome_bairro)
VALUES ('Agroindustrial');
INSERT INTO bairros (nome_bairro)
VALUES ('Arroio do Só');
INSERT INTO bairros (nome_bairro)
VALUES ('Cerrito');
INSERT INTO bairros (nome_bairro)
VALUES ('Boi Morto');
INSERT INTO bairros (nome_bairro)
VALUES ('Bonfim');
INSERT INTO bairros (nome_bairro)
VALUES ('Campestre do Menino Deus');
INSERT INTO bairros (nome_bairro)
VALUES ('Camobi');
INSERT INTO bairros (nome_bairro)
VALUES ('Carolina');
INSERT INTO bairros (nome_bairro)
VALUES ('Caturrita');
INSERT INTO bairros (nome_bairro)
VALUES ('Centro');
INSERT INTO bairros (nome_bairro)
VALUES ('Chácara das Flores');
INSERT INTO bairros (nome_bairro)
VALUES ('Divina Providência');
INSERT INTO bairros (nome_bairro)
VALUES ('Dom Antônio Reis');
INSERT INTO bairros (nome_bairro)
VALUES ('Duque de Caxias');
INSERT INTO bairros (nome_bairro)
VALUES ('Itararé');
INSERT INTO bairros (nome_bairro)
VALUES ('Juscelino Kubitschek');
INSERT INTO bairros (nome_bairro)
VALUES ('Lorenzi');
INSERT INTO bairros (nome_bairro)
VALUES ('Menino Jesus');
INSERT INTO bairros (nome_bairro)
VALUES ('Nonoai');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora das Dores');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora de Fátima');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora de Lourdes');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora Medianeira');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora do Perpétuo Socorro');
INSERT INTO bairros (nome_bairro)
VALUES ('Nossa Senhora do Rosário');
INSERT INTO bairros (nome_bairro)
VALUES ('Passo d''Areia');
INSERT INTO bairros (nome_bairro)
VALUES ('Patronato');
INSERT INTO bairros (nome_bairro)
VALUES ('Pinheiro Machado');
INSERT INTO bairros (nome_bairro)
VALUES ('Presidente João Goulart');
INSERT INTO bairros (nome_bairro)
VALUES ('Renascença');
INSERT INTO bairros (nome_bairro)
VALUES ('Rosário');
INSERT INTO bairros (nome_bairro)
VALUES ('Salgado Filho');
INSERT INTO bairros (nome_bairro)
VALUES ('Santo Antão');
INSERT INTO bairros (nome_bairro)
VALUES ('Santo Antônio');
INSERT INTO bairros (nome_bairro)
VALUES ('São João');
INSERT INTO bairros (nome_bairro)
VALUES ('São José');
INSERT INTO bairros (nome_bairro)
VALUES ('São Martinho');
INSERT INTO bairros (nome_bairro)
VALUES ('São Miguel');
INSERT INTO bairros (nome_bairro)
VALUES ('São Pedro');
INSERT INTO bairros (nome_bairro)
VALUES ('Tancredo Neves');
INSERT INTO bairros (nome_bairro)
VALUES ('Uglione');
INSERT INTO bairros (nome_bairro)
VALUES ('Urlândia');
INSERT INTO bairros (nome_bairro)
VALUES ('Vila Noal');
INSERT INTO bairros (nome_bairro)
VALUES ('Vila Oliveira');
INSERT INTO bairros (nome_bairro)
VALUES ('Vila Rica');
INSERT INTO bairros (nome_bairro)
VALUES ('Vila Schirmer');
INSERT INTO bairros (nome_bairro)
VALUES ('Perpétuo Socorro');
INSERT INTO bairros (nome_bairro)
VALUES ('Parque Pinheiro Machado');

-- Dados de exemplo para Serviços Adicionais
INSERT INTO servicos_adicionais (nome_servico)
VALUES ('Ajudante para Carga e Descarga');
INSERT INTO servicos_adicionais (nome_servico)
VALUES ('Montagem/Desmontagem de Móveis');
INSERT INTO servicos_adicionais (nome_servico)
VALUES ('Embalagem de Itens');
INSERT INTO servicos_adicionais (nome_servico)
VALUES ('Transporte de Itens Especiais');
