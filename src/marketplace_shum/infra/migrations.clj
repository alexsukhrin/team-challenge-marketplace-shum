(ns marketplace-shum.infra.migrations
  (:require
   [mount.core :refer [defstate]]
   [aero.core :refer [read-config]]
   [clojure.java.io :as io]
   [marketplace-shum.infra.db :as db]))

(defn files []
  (let [dir (io/file (io/resource "schemas"))]
    (->> (.listFiles dir)
         (filter #(and (.isFile %) (.endsWith (.getName %) ".edn")))
         (map #(.getPath %)))))

(defn load-schema [schema]
  (read-config (io/file schema)))

(defn migrate []
  (for [f (files)]
    (db/tx! (load-schema f))))

(defstate migratus :start (migrate))
