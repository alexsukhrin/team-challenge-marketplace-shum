(ns migrate
  (:require [datomic.api :as d]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [aero.core :as aero]))

(defn ^:exec main [& _]
  (let [env (or (System/getenv "APP_ENV") "dev")
        config (aero/read-config (str "config/" env ".edn"))
        uri (get-in config [:datomic :db-uri])
        _ (try (d/create-database uri) (catch Exception _ nil))
        conn (d/connect uri)
        schema-dir "resources/schema"
        schema-files (->> (io/file schema-dir)
                          file-seq
                          (filter #(.isFile %))
                          (filter #(string/ends-with? (.getName %) ".edn"))
                          (map #(.getPath %)))]
    (doseq [file schema-files]
      (let [schema (edn/read-string (slurp file))]
        (println "Applying schema from" file)
        (d/transact conn schema)))
    (println "✅ All schemas applied!")))