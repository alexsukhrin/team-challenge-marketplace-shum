(ns team-challenge.migrate
  (:require [datomic.api :as d]
            [team-challenge.config :as config]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:gen-class))

(defn- apply-schemas
  "Apply all schemas from resources/schema"
  [conn]
  (let [schema-dir (io/file (io/resource "schema"))
        files (file-seq schema-dir)]
    (doseq [file files]
      (when (.isFile file)
        (println "Applying schema from" (.getName file))
        (let [schema (edn/read-string (slurp file))
              _ (d/transact conn schema)]
          (println "✅ Schema applied from" (.getName file)))))))

(defn migrate 
  "Start migration" 
  []
  (println "--- Starting Datomic migration ---")
  (let [uri (get-in config/*config* [:datomic :db-uri])]
    (println "Datomic URI:" uri)
    (println "Creating database (if it does not exist)...")
    (d/create-database uri)
    (println "Database created or already exists.")
    (println "Connecting to database...")
    (let [conn (d/connect uri)]
      (println "Connection successful. Applying schemas...")
      (apply-schemas conn)
      (println "--- Datomic migration finished ---"))))
