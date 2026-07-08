# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Estágio 1 - build (Maven + JDK 21)
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Baixa dependências primeiro para aproveitar o cache de camadas do Docker
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Compila e empacota. Os testes de verdade (unitários + integração com
# Testcontainers) rodam na CI, não aqui: este estágio de build não tem acesso
# ao socket do Docker, então um teste que precise subir um container
# (Testcontainers) falharia de qualquer forma dentro do `docker build`.
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Estágio 2 - runtime (apenas JRE 21, imagem menor e sem ferramentas de build)
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# curl só para o HEALTHCHECK abaixo (consulta o endpoint do Actuator).
# Instalado e com cache do apt limpo na mesma camada para não inflar a imagem.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Executa como usuário sem privilégios (boa prática de segurança)
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /workspace/target/*.jar app.jar
RUN chown spring:spring /app/app.jar
USER spring

EXPOSE 8080

# Flags de JVM sensatas para rodar em container: usa um percentual da memória
# do container (em vez de detectar errado a RAM do host) e derruba o processo
# imediatamente em caso de OutOfMemoryError, para o orquestrador reiniciar em
# vez de deixar a aplicação travada num estado inconsistente. Pode ser
# sobrescrito em tempo de execução com `-e JAVA_OPTS="..."`.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Consulta o endpoint de saúde do Actuator (já exposto em application.yml:
# management.endpoints.web.exposure.include=health,info). O orquestrador
# (docker-compose, Render, ECS, k8s...) usa isso para saber quando o container
# está pronto para receber tráfego e para reiniciá-lo se travar.
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Forma shell + `exec` de propósito: permite expandir $JAVA_OPTS (variável de
# ambiente) E, por causa do `exec`, o processo java vira o PID 1 do container,
# recebendo SIGTERM diretamente — sem isso, o Spring Boot não teria chance de
# fazer o graceful shutdown antes do container ser morto à força.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
