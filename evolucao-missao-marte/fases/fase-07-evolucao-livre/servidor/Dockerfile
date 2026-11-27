# Imagem do servidor para o Render (ou qualquer provedor de contêiner).
# Etapa 1: compila com o wrapper do projeto e JDK 17.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

# Etapa 2: só o JRE e o JAR, rodando como usuário sem privilégios.
FROM eclipse-temurin:17-jre
RUN useradd --system --create-home missaomarte
WORKDIR /app
# Pasta gravável pelo usuário do serviço (H2 local, quando não há DATABASE_URL).
RUN chown missaomarte /app
COPY --from=build /app/build/libs/app.jar app.jar
USER missaomarte
# Aceita conexões de fora do contêiner; a porta vem de PORT (Render). Memória do plano gratuito: 512 MB.
ENV MISSAOMARTE_BIND=0.0.0.0 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
