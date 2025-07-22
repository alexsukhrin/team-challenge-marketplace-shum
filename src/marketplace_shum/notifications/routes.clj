(ns marketplace-shum.notifications.routes
  (:require [marketplace-shum.notifications.handler :as noti-handler]
            [marketplace-shum.web.middlewares :as middleware]))

(def routes
  ["/notifications"
   {:tags ["notifications"]}

   [""
    {:get {:summary "start websocket connection for notifications"
           :responses {200 {:body string?}}
           :middleware [middleware/wrap-authentication]
           :handler #'noti-handler/notifications-handler}}]])
