(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.client.api :as d]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn- load-schemas []
  (let [schema-dir (io/file (io/resource "schema"))]
    (->> (file-seq schema-dir)
         (filter #(.isFile %))
         (mapcat (comp edn/read-string slurp)))))

(defstate client
  :start (d/client (:datomic config/*config*))
  :stop nil)

(defstate conn
  :start (let [db-name (get-in config/*config* [:datomic :db-name])]
           (d/create-database client {:db-name db-name})
           (let [connection (d/connect client {:db-name db-name})
                 schemas (load-schemas)]
             (d/transact connection {:tx-data schemas})
             connection)))

(comment
  ;; To test manually in the REPL:
  ;; 1. (mount/start #'config/*config*)
  ;; 2. Evaluate the forms below one by one
  (def c (d/client (:datomic config/*config*)))
  (def db-name (get-in config/*config* [:datomic :db-name]))
  (d/create-database c {:db-name db-name})
  (def connection (d/connect c {:db-name db-name}))
  (def schemas (load-schemas))
  (d/transact connection {:tx-data schemas}))