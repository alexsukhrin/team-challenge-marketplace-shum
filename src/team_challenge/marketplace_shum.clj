(ns team-challenge.marketplace-shum
  (:require [mount.core :as mount]
            [team-challenge.config :as config]
            [team-challenge.web :as web]
            [team-challenge.db :as db])
  (:gen-class))

(defn start-config []
  (mount/start #'config/*config*))

(defn start-db-conn []
  (mount/start #'db/conn))

(defn start-server []
  (mount/start #'web/http-server))

(defn start-system []
  (start-config)
  (start-db-conn)
  (start-server))

(defn stop-config []
  (mount/stop #'config/*config*))

(defn stop-db-conn []
  (mount/stop #'db/conn))

(defn stop-server []
  (mount/stop #'web/http-server))

(defn stop-system []
  (stop-server)
  (stop-db-conn)
  (stop-config))

(defn -main
  "Start system"
  []
  (start-system))

(comment

  (mount/running-states)

  (start-system)
  (stop-system)

  (require '[datomic.api :as d])


  (def uri "datomic:sql://shum-prod?jdbc:postgresql://postgres-instance.cby2c0iga8z1.eu-central-1.rds.amazonaws.com:5432/marketplace?ssl=true&sslmode=require")

  (def uri "datomic:sql://shum-prod?jdbc:postgresql://localhost:5432/marketplace")

  (def uri "datomic:sql://shum-prod?jdbc:postgresql://localhost:5432/marketplace?user=marketplace_user&password=marketplace_password")

  (d/create-database uri)

  )
