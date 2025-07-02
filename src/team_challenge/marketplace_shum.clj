(ns team-challenge.marketplace-shum
  (:require [mount.core :as mount]
            [team-challenge.config :as config]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.migrate :as migrate]
            [team-challenge.service.s3-service :as s3])
  (:gen-class))

(defn start-config []
  (mount/start #'config/*config*))

(defn run-migrations []
  (mount/start #'migrate/migrations))

(defn start-db-conn []
  (mount/start #'db/datasource))

(defn start-s3 []
  (mount/start #'s3/s3))

(defn start-server []
  (mount/start #'web/http-server))

(defn start-system []
  (start-config)
  (run-migrations)
  (start-db-conn)
  (start-s3)
  (start-server))

(defn stop-config []
  (mount/stop #'config/*config*))

(defn stop-migrations []
  (mount/stop #'migrate/migrations))

(defn stop-db-conn []
  (mount/stop #'db/datasource))

(defn stop-s3 []
  (mount/stop #'s3/s3))

(defn stop-server []
  (mount/stop #'web/http-server))

(defn stop-system []
  (stop-server)
  (stop-db-conn)
  (stop-migrations)
  (stop-s3)
  (stop-config))

(defn -main
  "Start system"
  []
  (start-system))

(comment

  (mount/running-states)

  (start-system)
  (stop-system))
