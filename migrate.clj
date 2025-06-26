(ns migrate
  (:require [datomic.api :as d]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [aero.core :as aero]))

(defn ^:exec main [& _]
  (println "--- Starting Datomic migration ---")
  (let [env (or (System/getenv "APP_ENV") "dev")
        _ (println "APP_ENV=" env)
        config-path (str "config/" env ".edn")
        _ (println "Reading config from" config-path)
        config (aero/read-config config-path)
        uri (get-in config [:datomic :db-uri])
        _ (println "Datomic URI:" uri)
        _ (try (do (println "Creating database if not exists...")
                   (d/create-database uri)
                   (println "Database created!"))
              (catch Exception e (println "Database may already exist or error:" (.getMessage e))))
        _ (println "Connecting to database...")
        conn (d/connect uri)
        schema-dir "resources/schema"
        schema-files (->> (io/file schema-dir)
                          file-seq
                          (filter #(.isFile %))
                          (filter #(string/ends-with? (.getName %) ".edn"))
                          (map #(.getPath %)))]
    (println "Found schema files:" schema-files)
    (doseq [file schema-files]
      (let [schema (edn/read-string (slurp file))]
        (println "Applying schema from" file)
        (d/transact conn schema)
        (println "Schema applied from" file)))
    (println "✅ All schemas applied!")))