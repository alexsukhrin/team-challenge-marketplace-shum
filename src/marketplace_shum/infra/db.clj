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

(comment
  (def db-name {:db-name (get-in config/*config* [:db :name])})
  (def client (d/client (:datomic config/*config*)))
  (d/list-databases client {})
  (def conn (d/connect client db-name))
  (def db (d/db conn))
  (d/create-database client db-name)
  (d/delete-database client db-name))