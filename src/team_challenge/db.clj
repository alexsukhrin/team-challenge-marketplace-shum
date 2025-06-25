(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.api :as d]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn- get-conn []
  (let [uri (get-in config/*config* [:datomic :db-uri])
        _ (d/create-database uri)
        conn (d/connect uri)
        schema-dir "resources/schema"
        schema-files (->> (io/file schema-dir)
                          file-seq
                          (filter #(.isFile %))
                          (filter #(str/ends-with? (.getName %) ".edn"))
                          (map #(.getPath %)))]
    (doseq [file schema-files]
      (let [schema (edn/read-string (slurp file))]
        (d/transact conn schema)))
    conn))

(defstate conn
  :start (get-conn)
  :stop (.release conn))

(comment
  (def uri (get-in config/*config* [:datomic :db-uri]))

  (d/create-database uri)

  (def uri (get-in config/*config* [:datomic :db-uri]))

  (def conn (d/connect uri))

  (def schema-dir "resources/schema")

  (def schema-files (->> (io/file schema-dir)
                         file-seq
                         (filter #(.isFile %))
                         (filter #(str/ends-with? (.getName %) ".edn"))
                         (map #(.getPath %))))

  (doseq [file schema-files]
    (let [schema (edn/read-string (slurp file))]
      (d/transact conn schema))))
