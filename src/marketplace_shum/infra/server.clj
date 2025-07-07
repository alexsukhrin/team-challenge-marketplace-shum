(ns marketplace-shum.infra.server
  (:require
   [mount.core :refer [defstate]]
   [org.httpkit.server :as http-kit]
   [marketplace-shum.infra.config :as config]
   [marketplace-shum.web.routes :refer [make-routes]]))

(defstate http-server
  :start (try
           (println "Start server...")
           (http-kit/run-server #'make-routes {:port (get-in config/*config* [:web-server :port])})
           (catch Exception e (str "caught exception: " (.getMessage e))))
  :stop (when http-server (println "Stopping server...") (http-server)))
