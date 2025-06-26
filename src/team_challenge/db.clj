(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.api :as d]))

(println "Full config:*config* =" config/*config*)
(println ":datomic block =" (:datomic config/*config*))
(println "DB_HOST=" (System/getenv "DB_HOST"))
(println "DB_USER=" (System/getenv "DB_USER"))
(println "DB_PASS=" (System/getenv "DB_PASS"))

(defstate conn
  :start (do
           (println "Datomic URI:" (get-in config/*config* [:datomic :db-uri]))
           (d/connect (get-in config/*config* [:datomic :db-uri])))
  :stop (.release conn))
