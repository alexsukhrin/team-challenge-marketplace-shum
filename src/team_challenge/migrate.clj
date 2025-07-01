(ns team-challenge.migrate
  (:require [migratus.core :as migratus]
            [team-challenge.config :as config]
            [mount.core :refer [defstate]]))

(defstate migrations
  :start (do
           (println "--- Starting database migration ---")
           (migratus/migrate (config/load-config "config/migratus.edn"))
           (println "--- Migration completed ---"))
  :stop nil)

(defn migrate []
  (mount.core/start #'migrations))

(defn rollback []
  (println "--- Rolling back migration ---")
  (migratus/rollback (config/load-config "config/migratus.edn"))
  (println "--- Rollback completed ---"))

(comment
  (def config (config/load-config "config/migratus.edn"))
  (migratus/create config "alter_products")
  (migrate)
  (rollback))
