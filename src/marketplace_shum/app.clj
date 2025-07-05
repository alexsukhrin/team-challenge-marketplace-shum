(ns marketplace-shum.app
  (:require
   [mount.core :as mount]
   [marketplace-shum.infra.config :as config]
   [marketplace-shum.infra.db :as db]
   [marketplace-shum.infra.migrations :as m]
   [marketplace-shum.infra.server :as s])
  (:gen-class))

(defn start-system []
  (mount/start #'config/*config*)
  (mount/start #'db/db)
  (mount/start #'m/migratus)
  (mount/start #'s/http-server))

(defn -main
  "Start system"
  []
  (start-system))

(comment
  ;; start
  (start-system)

  ;; stats 
  (mount/running-states))
