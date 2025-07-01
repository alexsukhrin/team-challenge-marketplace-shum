CREATE DATABASE datomic
  WITH OWNER = marketplace_user
       TEMPLATE template0
       ENCODING = 'UTF8'
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

\connect datomic

CREATE TABLE datomic_kvs (
  id  TEXT NOT NULL,
  rev INTEGER,
  map TEXT,
  val BYTEA,
  CONSTRAINT pk_id PRIMARY KEY (id)
);

ALTER TABLE datomic_kvs OWNER TO marketplace_user;
GRANT ALL ON TABLE datomic_kvs TO marketplace_user;
GRANT ALL ON TABLE datomic_kvs TO public;

CREATE ROLE datomic LOGIN PASSWORD 'datomic';
