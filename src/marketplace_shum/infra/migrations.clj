(ns marketplace-shum.infra.migrations
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [marketplace-shum.infra.db :as db]))

(defn files []
  (let [schemas-dir (io/resource "schemas")
        schemas-url (io/as-url schemas-dir)]
    (if (= "jar" (.getProtocol schemas-url))
      ;; В JAR файлі - читаємо всі .edn файли з schemas
      (let [jar-connection (.openConnection schemas-url)
            jar-file (-> jar-connection .getJarFileURL io/as-url .getFile)]
        (with-open [jar (java.util.jar.JarFile. jar-file)]
          (->> (.entries jar)
               (filter #(and (.startsWith (.getName %) "schemas/")
                            (.endsWith (.getName %) ".edn")))
               (map #(.getName %)))))
      ;; Для файлової системи (розробка)
      (let [dir (io/file schemas-dir)]
        (if (.isDirectory dir)
          (->> (.listFiles dir)
               (filter #(and (.isFile %) (.endsWith (.getName %) ".edn")))
               (map #(.getPath %)))
          [])))))

(defn load-schema [schema-path]
  (if (.startsWith schema-path "schemas/")
    ;; Для JAR ресурсів
    (read-config (io/resource schema-path))
    ;; Для файлів файлової системи
    (read-config (io/file schema-path))))

(defn migrate []
  (for [f (files)]
    (db/tx! (load-schema f))))

(defstate migratus :start (migrate))
