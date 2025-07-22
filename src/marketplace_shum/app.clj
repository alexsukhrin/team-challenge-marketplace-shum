(ns marketplace-shum.app
  (:require
   [mount.core :as mount]
   [marketplace-shum.infra.config :as config]
   [marketplace-shum.infra.db :as db]
   [marketplace-shum.infra.migrations :as m]
   [marketplace-shum.infra.server :as s])
  (:gen-class))

(defn -main
  "Start system"
  []
  (prn "Start system")
  (mount/start))

(comment
  (mount/start #'config/*config*)
  (mount/start #'db/db)
  (mount/start #'m/migratus)
  (mount/start #'s/http-server)

  (mount/stop #'s/http-server)

  (mount/stop)

  ;; stats 
  (mount/running-states))
