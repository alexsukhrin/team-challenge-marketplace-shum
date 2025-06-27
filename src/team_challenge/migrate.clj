(ns team-challenge.migrate
  (:require [datomic.api :as d]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [aero.core :as aero])
  (:gen-class))

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
       (filter #(string/ends-with? (.getName %) ".edn"))
       (map #(.getPath %))))

(defn apply-schemas [conn schema-files]
  (doseq [file schema-files]
    (println "📄 Applying schema from" file)
    (let [schema (edn/read-string (slurp file))
          _ (d/transact conn schema)]
      (println "✅ Schema applied from" file))))

(defn -main 
  "Start migration"
  []
  (println "--- Starting Datomic migration ---")
  (let [env (or (System/getenv "APP_ENV") "dev")
        _ (println "APP_ENV=" env)
        config-path (str "config/" env ".edn")
        _ (println "Reading config from" config-path)
        config (aero/read-config config-path)
        uri (get-in config [:datomic :db-uri])
        _ (println "Datomic URI:" uri)]
    (create-db-if-needed uri)
    (println "Connecting to database...")
    (let [conn (d/connect uri)
          schema-dir "resources/schema"
          schema-files (load-schema-files schema-dir)]
      (println "Found schema files:" schema-files)
      (apply-schemas conn schema-files)
      (println "✅ All schemas applied!"))))
