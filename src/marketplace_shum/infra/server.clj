(ns marketplace-shum.infra.server
  (:require
   [mount.core :refer [defstate]]
   [org.httpkit.server :as http-kit]
   [marketplace-shum.infra.config :as config]
   [marketplace-shum.web.routes :refer [make-routes]]))

(defstate http-server
  :start (http-kit/run-server #'make-routes {:port (get-in config/*config* [:web-server :port])})
  :stop (when http-server (println "Stopping server...") (http-server)))
