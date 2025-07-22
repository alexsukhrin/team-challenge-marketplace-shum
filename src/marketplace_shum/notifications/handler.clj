(ns marketplace-shum.notifications.handler
  (:require [cheshire.core :as json]
            [marketplace-shum.notifications.service :as noti-service]
            [org.httpkit.server :as http]
            [clojure.core :as lib]))

(defn notifications-handler [request]
  (http/with-channel request channel
    (let [{:keys [user]} request
          user-id (:user-id user)]
      (println "WS connected for user" user-id)
      (noti-service/add-new-connection! user-id channel)

      (let [notis (noti-service/get-notifications user-id)
            payload {:type "init"
                     :notifications (map first notis)}]
        (http/send! channel (json/generate-string payload)))

      (http/on-receive channel
                       (fn [msg]
                         (try
                           (let [{:strs [type message notification-id]} (json/parse-string msg)]
                             (case type
                               "new"
                               (let [notif-id (noti-service/create-notification {:message message :user-id user-id :read? false})]
                                 (http/send! channel (json/generate-string {:type "ack" :id (str notif-id)})))

                               "mark-read"
                               (do
                                 (noti-service/mark-notification-read (lib/parse-uuid  notification-id))
                                 (http/send! channel (json/generate-string {:type "marked-read"
                                                                            :id notification-id})))

                               (http/send! channel (json/generate-string {:type "error"
                                                                          :error "Unknown type"}))))
                           (catch Exception _
                             (http/send! channel (json/generate-string {:type "error"
                                                                        :error "Invalid JSON"}))))))

      (http/on-close channel
                     (fn [status]
                       (println "WS disconnected:" status)
                       (noti-service/remove-connection! user-id))))))
