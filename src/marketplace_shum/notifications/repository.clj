(ns marketplace-shum.notifications.repository
  (:require [datomic.client.api :as d]))

(defn create-notification
  [conn {:keys [message user-id read?]}]
  (let [notification-id (random-uuid)]
    (d/transact conn [{:notification/id notification-id
                       :notification/message message
                       :notification/read? read?
                       :notification/user [:user/id user-id]}])
    notification-id))

(defn get-notifications
  [db user-id]
  (d/q '[:find (pull ?e [*])
         :in $ ?user-id
         :where [?e :notification/user ?u]
         [?u :user/id ?user-id]]
       (d/db db) user-id))

(defn mark-notification-read!
  [conn notification-id]
  (d/transact conn [{:notification/id notification-id
                     :notification/read? true}]))
