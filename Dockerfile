# Етап 1: build uberjar
FROM clojure:openjdk-17-tools-deps AS builder
WORKDIR /app
COPY deps.edn .
COPY build.clj .
COPY src ./src
COPY resources ./resources
COPY config ./config
RUN clojure -T:uberjar

# Етап 2: мінімальний runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/app.jar ./app.jar

# Виставляємо ліміти JVM (можна перевизначити через docker run)
EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
