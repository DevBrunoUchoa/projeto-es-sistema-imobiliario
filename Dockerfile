# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Estágio 1 - build (Maven + JDK 21)
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Baixa dependências primeiro para aproveitar o cache de camadas do Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compila e empacota (os testes rodam na CI; aqui geramos o artefato)
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Estágio 2 - runtime (apenas JRE 21, imagem menor e sem ferramentas de build)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Executa como usuário sem privilégios (boa prática de segurança)
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
