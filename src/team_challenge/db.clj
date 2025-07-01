(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [team-challenge.migrate :refer [migrate]]
            [datomic.api :as d]))

(defstate conn
  :start (let [uri (get-in config/*config* [:datomic :db-uri])]
           (println "Datomic URI:" uri)
           (migrate)
           (println "Connecting to database...")
           (d/connect uri))
  :stop (when conn (.release conn)))
