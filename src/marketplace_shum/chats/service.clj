(ns marketplace-shum.chats.service
  (:require [marketplace-shum.chats.repository :as repo]
            [marketplace-shum.infra.db :refer [db]]))

(defn create-chat [user-ids & {:keys [name avatar-url]}]
  (repo/create-chat! db user-ids :name name :avatar-url avatar-url))

(defn add-user-to-chat [chat-id user-id]
  (repo/add-user-to-chat! db chat-id user-id))

(defn remove-user-from-chat [chat-id user-id]
  (repo/remove-user-from-chat! db chat-id user-id))

(defn pin-message [chat-id message-id]
  (repo/pin-message! db chat-id message-id))

(defn unpin-message [chat-id message-id]
  (repo/unpin-message! db chat-id message-id))

(defn add-message [chat-id sender-id text & opts]
  (apply repo/add-message! db chat-id sender-id text opts))

(defn forward-message [chat-id sender-id orig-message-id]
  (repo/forward-message! db chat-id sender-id orig-message-id))

(defn reply-message [chat-id sender-id text reply-to-id & opts]
  (apply repo/reply-message! db chat-id sender-id text reply-to-id opts))

(defn delete-message [message-id]
  (repo/delete-message! db message-id))

(defn get-chats-for-user [user-id]
  (repo/get-chats-for-user db user-id))

(defn get-messages-for-chat [chat-id]
  (repo/get-messages-for-chat db chat-id))

(defn unread-counts [user-id]
  (repo/unread-counts db user-id))

(defn mark-chat-read [chat-id user-id]
  (repo/mark-chat-read! db chat-id user-id)) 