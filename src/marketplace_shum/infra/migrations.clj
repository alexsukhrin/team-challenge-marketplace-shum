(ns marketplace-shum.infra.migrations
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [datomic.client.api :as d]
   [marketplace-shum.infra.db :refer [db]]))

(defn files []
  (let [dir "schemas"
        cl (.getContextClassLoader (Thread/currentThread))
        urls (enumeration-seq (.getResources cl dir))]
    (mapcat
     (fn [url]
       (let [conn (.openConnection url)]
         (if (instance? java.net.JarURLConnection conn)
           (let [jar-file (.getJarFile ^java.net.JarURLConnection conn)]
             (->> (enumeration-seq (.entries jar-file))
                  (map #(.getName %))
                  (filter #(and (.startsWith % (str dir "/"))
                                (.endsWith % ".edn")))))
            ;; dev-режим
           (let [f (io/file (io/resource dir))]
             (->> (.listFiles f)
                  (filter #(and (.isFile %) (.endsWith (.getName %) ".edn")))
                  (map #(.getPath %)))))))
     urls)))

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
