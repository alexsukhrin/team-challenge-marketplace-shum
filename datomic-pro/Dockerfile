FROM eclipse-temurin:11-jre

WORKDIR /opt/datomic

COPY datomic-pro-1.0.7364.zip .
COPY postgresql-42.7.1.jar .

RUN apt-get update && apt-get install -y unzip && \
    unzip datomic-pro-1.0.7364.zip && \
    rm datomic-pro-1.0.7364.zip

WORKDIR /opt/datomic/datomic-pro-1.0.7364

# Download PostgreSQL JDBC driver if not present
RUN if [ ! -f postgresql-42.7.1.jar ]; then \
    curl -L -o postgresql-42.7.1.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.1/postgresql-42.7.1.jar; \
  fi

EXPOSE 8998

CMD ["bin/transactor", "config/transactor.properties"]