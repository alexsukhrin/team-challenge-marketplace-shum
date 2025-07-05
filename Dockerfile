# --- Stage 1: Build the app ---
FROM clojure:openjdk-17-tools-deps AS builder
WORKDIR /app

# Copy project
COPY deps.edn .
COPY build.clj .
COPY src ./src
COPY resources ./resources
COPY config ./config

# Build main jar
RUN clojure -T:uberjar :uber-file '"target/app.jar"'

# --- Stage 2: Minimal runtime image ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy built artifacts
COPY --from=builder /app/target/app.jar ./app.jar
COPY --from=builder /app/config/ ./config
COPY --from=builder /app/resources/ ./resources

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
