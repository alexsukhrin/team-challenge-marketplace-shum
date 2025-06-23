(ns team-challenge.marketplace-shum
  (:require [mount.core :as mount]
            [team-challenge.config :as config]
            [team-challenge.web :as web]
            [team-challenge.db :as db]
            [reitit.ring :as ring])
  (:gen-class))

(defn start-config []
  (mount/start #'config/*config*))

(defn start-db-client []
  (mount/start #'team-challenge.db/client))

(defn start-db-conn []
  (mount/start #'db/conn))

(defn start-server []
  (mount/start #'web/http-server))

(defn start-system []
  (start-config)
  (start-db-client)
  (start-db-conn)
  (start-server))

(defn stop-config []
  (mount/stop #'config/*config*))

(defn stop-db-client []
  (mount/stop #'team-challenge.db/client))

(defn stop-db-conn []
  (mount/stop #'db/conn))

(defn stop-server []
  (mount/stop #'web/http-server))

(defn stop-system []
  (stop-server)
  (stop-db-conn)
  (stop-db-client)
  (stop-config))

(defn -main
  "Start system"
  []
  (start-system))

(comment

  (mount/running-states)

  (start-system)
  (stop-system)
  )