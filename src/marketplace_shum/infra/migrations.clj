(ns marketplace-shum.infra.migrations
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [datomic.client.api :as d]
   [marketplace-shum.infra.db :refer [db]]))

(defn files []
  (let [schemas-dir (io/resource "schemas")]
    (println "[migrations] schemas-dir:" schemas-dir)
    (if (nil? schemas-dir)
      (do (println "[migrations] WARNING: schemas resource not found!")
          [])
      (let [schemas-url (io/as-url schemas-dir)]
        (if (= "jar" (.getProtocol schemas-url))
          (let [jar-connection (.openConnection schemas-url)
                jar-file (-> jar-connection .getJarFileURL io/as-url .getFile)]
            (with-open [jar (java.util.jar.JarFile. jar-file)]
              (->> (.entries jar)
                   (filter #(and (.startsWith (.getName %) "schemas/")
                                 (.endsWith (.getName %) ".edn")))
                   (map #(.getName %)))))
          (let [dir (io/file schemas-dir)]
            (println "[migrations] local dir:" (.getAbsolutePath dir))
            (if (.isDirectory dir)
              (->> (.listFiles dir)
                   (filter #(and (.isFile %) (.endsWith (.getName %) ".edn")))
                   (map #(.getPath %)))
              (do (println "[migrations] WARNING: schemas directory not found or not a directory!")
                  []))))))))

(defn load-schema [schema-path]
  (if (.startsWith schema-path "schemas/")
    (read-config (io/resource schema-path))
    (read-config (io/file schema-path))))

(defn migrate []
  (doseq [f (files)]
    (prn (str "Migrate -> " f)
         (d/transact db {:tx-data (load-schema f)}))))

(defstate migratus :start (try
                            (println "Start migrations...")
                            (migrate)
                            (catch Exception e (str "caught exception: " (.getMessage e)))))
