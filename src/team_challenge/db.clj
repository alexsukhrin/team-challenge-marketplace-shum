(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.api :as d]))

(defstate conn
  :start (let [uri (get-in config/*config* [:datomic :db-uri])]
           (println "Datomic URI:" uri)
           (println "Connecting to database...")
           (d/connect uri))
  :stop (when conn (.release conn)))
