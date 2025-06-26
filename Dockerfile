# Етап збірки
FROM clojure:openjdk-17-tools-deps AS build
WORKDIR /app

# Копіюємо залежності для кешування
COPY deps.edn build.clj ./
RUN clj -P

# Копіюємо решту проєкту
COPY src ./src
COPY resources ./resources
COPY config ./config
COPY migrate.clj ./

# Створюємо uberjar
RUN clojure -T:uberjar

# Етап запуску
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копіюємо зібрані файли
COPY --from=build /app/target/app.jar ./app.jar
COPY --from=build /app/config ./config
COPY --from=build /app/resources ./resources
COPY --from=build /app/migrate.clj ./migrate.clj
COPY --from=build /app/src ./src
COPY entrypoint.sh ./entrypoint.sh
RUN apk add --no-cache bash curl && \
    curl -O https://download.clojure.org/install/linux-install-1.11.1.1273.sh && \
    chmod +x linux-install-1.11.1.1273.sh && \
    ./linux-install-1.11.1.1273.sh && \
    rm linux-install-1.11.1.1273.sh && \
    chmod +x entrypoint.sh

ENV JAVA_OPTS="-Xmx256m -Ddatomic.objectCacheMax=32m -Ddatomic.memoryIndexMax=64m"

ENTRYPOINT ["./entrypoint.sh"]
