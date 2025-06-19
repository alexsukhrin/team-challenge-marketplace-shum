(ns migration
  (:require [migratus.core :as migratus]
            [aero.core :refer [read-config]]))

(defn env!
  ([varname]
   (or (System/getenv varname)
       (throw (RuntimeException.
               (str "Missing required environment variable: " varname)))))
  ([varname default]
   (or (System/getenv varname)
       default)))

(def config
  (read-config (str "config/" (env! "APP_ENV" "dev") ".edn")))

(defn migratus-config []
  {:store :database
   :migration-dir "migrations/"
   :migration-table-name "migrations"
   :db (-> config :db :migratus)})

(defn init []
  (migratus/init (migratus-config)))

(defn migrate! []
  (migratus/migrate (migratus-config)))

(defn rollback! []
  (migratus/rollback (migratus-config)))

(defn create [name]
  (migratus/create config name))

(comment
  
  (init)
  
  (create "users")
  )