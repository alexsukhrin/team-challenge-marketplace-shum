(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.api :as d]))

(defstate conn
  :start (d/connect (get-in config/*config* [:datomic :db-uri]))
  :stop (.release conn))
