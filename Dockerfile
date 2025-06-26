FROM clojure:openjdk-17-tools-deps AS builder
WORKDIR /app
COPY deps.edn .
COPY build.clj .
COPY src ./src
COPY resources ./resources
COPY config ./config
RUN clojure -T:uberjar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/app.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
