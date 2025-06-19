(ns team-challenge.marketplace-shum
  (:require [mount.core :as mount]
            [team-challenge.config :as config]
            [team-challenge.db :as db])
  (:gen-class))

(defn start-config []
  (mount/start #'team-challenge.config/*config*))

(defn stop-config []
  (mount/stop config/*config*))

(defn start-db []
  (mount/start #'team-challenge.db/*db*))

(defn stop-db []
  (mount/stop #'team-challenge.db/*db*))

(defn start-system []
  (start-config)
  (start-db))

(comment

  (start-config)
  (start-db))

(defn -main
  "Start system"
  []
  (start-system))
