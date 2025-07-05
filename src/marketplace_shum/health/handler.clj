(ns marketplace-shum.health.handler)

(defn ping_handler [_]
  {:status 200
   :body {:message "pong"}})

(def route
  ["/ping" {:get {:summary "health check system"
                  :responses {200 {:body {:message string?}}}
                  :handler #'ping_handler}}])