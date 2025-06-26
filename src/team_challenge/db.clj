(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [datomic.api :as d]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn create-db-if-needed [uri]
  (try
    (println "Creating database if not exists...")
    (d/create-database uri)
    (println "✅ Database created.")
    (catch Exception e
      (println "⚠️ Database may already exist or error:" (.getMessage e)))))

(defn load-schema-files [dir]
  (->> (io/file dir)
       file-seq
       (filter #(.isFile %))
       (filter #(str/ends-with? (.getName %) ".edn"))
       (map #(.getPath %))))

(defn apply-schemas [conn schema-files]
  (doseq [file schema-files]
    (println "📄 Applying schema from" file)
    (let [schema (edn/read-string (slurp file))
          _ (d/transact conn schema)]
      (println "✅ Schema applied from" file))))

(defstate conn
  :start (let [uri (get-in config/*config* [:datomic :db-uri])]
           (println "Datomic URI:" uri)
           (create-db-if-needed uri)
           (println "Connecting to database...")
           (let [conn (d/connect uri)
                 schema-dir "resources/schema"
                 schema-files (load-schema-files schema-dir)]
             (println "Found schema files:" schema-files)
             (apply-schemas conn schema-files)
             conn))
  :stop (when conn (.release conn)))

