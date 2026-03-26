-- Sample seed data loaded on application start
INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES
  ('Configurar ambiente de desenvolvimento', 'Instalar JDK 17, Maven e IDE (IntelliJ / VS Code).', 'DONE', NOW(), NOW()),
  ('Criar estrutura MVC do projeto', 'Definir pacotes controller, service, repository e model.', 'DONE', NOW(), NOW()),
  ('Implementar CRUD de Tarefas', 'Criar endpoints para criar, listar, editar e remover tarefas.', 'IN_PROGRESS', NOW(), NOW()),
  ('Estilizar a interface com Bootstrap', 'Adicionar folhas de estilo e componentes visuais responsivos.', 'IN_PROGRESS', NOW(), NOW()),
  ('Escrever testes unitários', 'Cobrir service e controller com JUnit 5 e Mockito.', 'PENDING', NOW(), NOW()),
  ('Publicar aplicação', 'Realizar deploy em servidor de produção.', 'PENDING', NOW(), NOW());
