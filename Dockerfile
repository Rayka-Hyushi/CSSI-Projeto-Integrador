# ===== STAGE 1: Build =====
# Imagem com Maven para compilar o projeto
FROM maven:3.9.9-eclipse-temurin-21 AS build

# Diretório de trabalho
WORKDIR /app

# Copia o pom.xml e o código fonte
COPY pom.xml .
COPY src ./src

# Compila o projeto (gera o JAR em target/) - pula testes e compilação de testes
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# ===== STAGE 2: Runtime =====
# Imagem leve apenas com Java 21 para rodar a app
FROM eclipse-temurin:21-jre-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR compilado do STAGE 1
COPY --from=build /app/target/*.jar app.jar

# Variáveis de ambiente (podem ser sobrescritas via docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/projeto_integrador
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=1234

# Expõe a porta 8080 para acesso externo
EXPOSE 8080

# Comando para iniciar a aplicação Java ao iniciar o container
ENTRYPOINT ["java", "-jar", "app.jar"]