# CSSI-Projeto-Integrador

Repositório para desenvolvimento da aplicação do Projeto Integrador com extensão, do curso de Graduação em Sistemas para Internet, cujo o desenvolvedor é Sidnei dos Santos.

---

## Tecnologias

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.2 |
| Padrão | MVC (Model-View-Controller) |
| View | Thymeleaf + Bootstrap 5 |
| Persistência | Spring Data JPA + H2 (in-memory) |
| Validação | Jakarta Bean Validation |
| Build | Apache Maven |

---

## Estrutura do Projeto (MVC)

```
src/
├── main/
│   ├── java/com/projetointegrador/
│   │   ├── ProjetoIntegradorApplication.java  ← Ponto de entrada
│   │   ├── controller/                        ← [C] Controladores MVC
│   │   │   ├── HomeController.java
│   │   │   └── TaskController.java
│   │   ├── model/                             ← [M] Entidades / domínio
│   │   │   ├── Task.java
│   │   │   └── TaskStatus.java
│   │   ├── repository/                        ← Acesso a dados (JPA)
│   │   │   └── TaskRepository.java
│   │   └── service/                           ← Regras de negócio
│   │       └── TaskService.java
│   └── resources/
│       ├── templates/                         ← [V] Views Thymeleaf
│       │   ├── fragments/layout.html          ←  Cabeçalho / rodapé
│       │   ├── index.html                     ←  Página inicial
│       │   └── tasks/
│       │       ├── list.html                  ←  Lista de tarefas
│       │       ├── form.html                  ←  Criar / Editar
│       │       └── detail.html               ←  Detalhes
│       ├── static/css/style.css               ←  Estilos customizados
│       ├── application.properties
│       └── data.sql                           ←  Dados de exemplo
└── test/
    └── java/com/projetointegrador/
        ├── ProjetoIntegradorApplicationTests.java
        ├── controller/TaskControllerTest.java
        └── service/TaskServiceTest.java
```

---

## Como Executar

### Pré-requisitos
- Java 17+
- Apache Maven 3.6+

### Rodar a aplicação

```bash
mvn spring-boot:run
```

Acesse: [http://localhost:8080](http://localhost:8080)

### Rodar os testes

```bash
mvn test
```

### Console H2 (banco em memória)

Acesse: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
- **JDBC URL:** `jdbc:h2:mem:projetodb`  
- **User:** `sa` / **Password:** *(vazio)*

---

## Funcionalidades (Sistema de Tarefas)

- ✅ Listar tarefas com filtro por status e busca por título
- ✅ Criar nova tarefa (título, descrição, status)
- ✅ Editar tarefa existente
- ✅ Ver detalhes de uma tarefa
- ✅ Excluir tarefa
- ✅ Dados de exemplo carregados automaticamente na inicialização

