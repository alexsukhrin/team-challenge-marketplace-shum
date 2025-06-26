FROM clojure:openjdk-17-tools-deps AS build
WORKDIR /app

# Копіюємо залежності для кешування
COPY deps.edn ./
COPY build.clj ./
COPY src ./src
COPY resources ./resources
COPY config ./config

RUN clj -P

# Збираємо uberjar
RUN clojure -T:build uber

# ---
# Runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/app.jar ./app.jar
COPY --from=build /app/config ./config
COPY --from=build /app/resources ./resources

ENV JAVA_OPTS="-Xmx256m -Ddatomic.objectCacheMax=32m -Ddatomic.memoryIndexMax=64m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]