# --- Stage 1: Datomic installation ---
FROM clojure:openjdk-17-tools-deps AS datomic
WORKDIR /datomic

RUN apt-get update && apt-get install -y unzip curl maven

RUN curl -L "https://datomic-pro-downloads.s3.amazonaws.com/1.0.7364/datomic-pro-1.0.7364.zip" -o datomic-pro-1.0.7364.zip \
      && unzip datomic-pro-1.0.7364.zip \
      && rm datomic-pro-1.0.7364.zip \
      && mvn install:install-file \
      -Dfile=datomic-pro-1.0.7364/peer-1.0.7364.jar \
      -DpomFile=datomic-pro-1.0.7364/pom.xml \
      -DgroupId=com.datomic \
      -DartifactId=datomic-pro \
      -Dversion=1.0.7364 \
      -Dpackaging=jar

# --- Stage 2: Build the app and migrate jars ---
FROM clojure:openjdk-17-tools-deps AS builder
WORKDIR /app

# Копіюємо проект
COPY deps.edn .
COPY build.clj .
COPY src ./src
COPY resources ./resources
COPY config ./config

# Копіюємо локальний Maven репозиторій з Datomic
COPY --from=datomic /root/.m2 /root/.m2

# Збірка основного jar
RUN clojure -T:uberjar :uber-file '"target/app.jar"'

# Збірка jar для міграцій
RUN clojure -T:uberjar-migrate

# --- Stage 3: Minimal runtime image ---
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копіюємо зібрані артефакти
COPY --from=builder /app/target/app.jar ./app.jar
COPY --from=builder /app/target/app-migrate.jar ./app-migrate.jar
COPY --from=builder /app/config/ ./config

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
      