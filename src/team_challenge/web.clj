(ns team-challenge.web
  (:require [mount.core :refer [defstate]]
            [org.httpkit.server :as http-kit]
            [team-challenge.config :as config]
            [team-challenge.api.routes :as routes]
            [reitit.ring :as ring]
            [ring.middleware.cors :as cors]))

(defstate http-server
  :start
  (let [port (get-in config/*config* [:web-server :port] 3000)
        app (ring/ring-handler (routes/make-routes))]
    (println (str "Starting server on port: " port))
    (http-kit/run-server
     app
     {:port port}))

  :stop
  (when http-server
    (println "Stopping server...")
    (http-server)))