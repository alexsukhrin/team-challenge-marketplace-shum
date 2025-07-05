(ns marketplace-shum.infra.db
  (:require
   [mount.core :refer [defstate]]
   [datomic.client.api :as d]
   [marketplace-shum.infra.config :as config]))

(defstate db
  :start (let [db-name {:db-name (get-in config/*config* [:db :name])}
               client (d/client (:datomic config/*config*))]
           (d/create-database client db-name)
           (d/connect client db-name)))

(defn tx! [data]
  (d/transact db data))
