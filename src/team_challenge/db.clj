(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [next.jdbc :as jdbc]))

(defstate datasource
  :start (let [db-spec (get-in config/*config* [:db :spec])]
           (println "Postgres DB spec:" db-spec)
           (jdbc/get-datasource db-spec))
  :stop nil)
