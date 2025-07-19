(ns marketplace-shum.infra.db
  (:require
   [mount.core :refer [defstate]]
   [datomic.client.api :as d]
   [marketplace-shum.infra.config :as config]))

(defstate db
  :start (let [db-name {:db-name (get-in config/*config* [:db :name])}
               client (d/client (:datomic config/*config*))]
           (println "Create db...")
           (d/create-database client db-name)
           (println "Create connect db...")
           (d/connect client db-name)))
