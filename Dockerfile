FROM clojure:openjdk-17-tools-deps AS builder
WORKDIR /app
COPY deps.edn .
COPY lib .
COPY build.clj .
COPY src ./src
COPY resources ./resources
COPY config ./config

RUN apt-get update && apt-get install -y unzip curl maven \
 && mkdir -p lib \
 && curl -L "https://datomic-pro-downloads.s3.amazonaws.com/1.0.7364/datomic-pro-1.0.7364.zip" -o lib/datomic-pro-1.0.7364.zip \
 && unzip lib/datomic-pro-1.0.7364.zip -d lib \
 && rm lib/datomic-pro-1.0.7364.zip \
 && mvn install:install-file \
      -Dfile=lib/datomic-pro-1.0.7364/peer-1.0.7364.jar \
      -DpomFile=lib/datomic-pro-1.0.7364/pom.xml \
      -DgroupId=com.datomic \
      -DartifactId=datomic-pro \
      -Dversion=1.0.7364 \
      -Dpackaging=jar

RUN clojure -T:uberjar
RUN clojure -T:uberjar-migrate

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/app.jar ./app.jar
COPY --from=builder /app/target/app-migrate.jar ./app-migrate.jar
COPY --from=builder /app/config/ ./config

EXPOSE 4000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
