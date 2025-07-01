(ns team-challenge.marketplace-shum
  (:require [mount.core :as mount]
            [team-challenge.config :as config]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [team-challenge.migrate :as migrate])
  (:gen-class))

(defn start-config []
  (mount/start #'config/*config*))

(defn run-migrations []
  (migrate/migrate))

(defn start-db-conn []
  (mount/start #'db/datasource))

(defn start-server []
  (mount/start #'web/http-server))

(defn start-system []
  (start-config)
  (run-migrations)
  (start-db-conn)
  (start-server))

(defn stop-config []
  (mount/stop #'config/*config*))

(defn stop-db-conn []
  (mount/stop #'db/datasource))

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
  (stop-system))
