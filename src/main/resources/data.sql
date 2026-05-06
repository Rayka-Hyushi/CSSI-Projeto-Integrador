INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Admin', 'admin@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u', '5555999999999', '000.000.000-00', 'ROLE_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Cliente', 'cliente@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u', '5555912345678', '100.023.040-00', 'ROLE_CLIENTE', CURRENT_TIMESTAMP);
INSERT INTO clientes (id_usuario) VALUES (2);

INSERT INTO usuarios (nome_completo, email, senha, whatsapp, cpf, tipo_usuario, created_at)
VALUES ('Prestador', 'prestador@mail.com', '$2a$12$/YbzkGaSCR5m18chYzmfc.h5Vo.X7pGhb7f5XIMY7.vxbgMmJgU2u', '555532147865', '010.203.040-00', 'ROLE_PRESTADOR', CURRENT_TIMESTAMP);
INSERT INTO prestadores (id_usuario) VALUES (3);