(ns marketplace-shum.notifications.service
  (:require [cheshire.core :as json]
            [org.httpkit.server :as http]
            [marketplace-shum.infra.db :refer [db]]
            [marketplace-shum.notifications.domain :as noti-domain]
            [marketplace-shum.notifications.repository :as noti-repo]))

(defonce connections (atom {}))

(defn add-new-connection!
  [user-id ch]
  (swap! connections assoc user-id ch))

(defn remove-connection!
  [user-id]
  (swap! connections dissoc user-id))

(defn send-notification!
  [user-id message]
  (when-let [channel (get @connections user-id)]
    (http/send! channel (json/generate-string
                         {:type "notification"
                          :message message}))))

(defn get-notifications
  [user-id]
  (noti-repo/get-notifications db user-id))

(defn create-notification
  [{:keys [message user-id read?]}]
  (if (noti-domain/valid-message? message)
    (noti-repo/create-notification db {:message message
                                       :user-id user-id
                                       :read? read?})
    (throw (ex-info "Invalid notification message" {:reason :bad-message}))))

(defn mark-notification-read
  [notification-id]
  (noti-repo/mark-notification-read! db notification-id))

(comment

  @connections

  (send-notification! "96675618-087b-4315-ae65-17c0cc74fd72"
                      {:type "notification" :message "Вам нове повідомлення!"}))