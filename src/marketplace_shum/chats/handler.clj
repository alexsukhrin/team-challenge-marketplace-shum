(ns marketplace-shum.chats.handler
  (:require [marketplace-shum.chats.service :as service]))

(defn get-chats-for-user-handler [{{:keys [user_id]} :path-params}]
  {:status 200 :body {:chats (service/get-chats-for-user user_id)}})

(defn create-chat-handler [{:keys [body]}]
  (let [{:keys [user_ids name avatar_url]} body]
    {:status 201 :body {:chat-id (service/create-chat user_ids :name name :avatar-url avatar_url)}}))

(defn add-user-to-chat-handler [{:keys [body]}]
  (let [{:keys [chat_id user_id]} body]
    (service/add-user-to-chat chat_id user_id)
    {:status 200 :body {:message "User added to chat"}}))

(defn remove-user-from-chat-handler [{:keys [body]}]
  (let [{:keys [chat_id user_id]} body]
    (service/remove-user-from-chat chat_id user_id)
    {:status 200 :body {:message "User removed from chat"}}))

(defn pin-message-handler [{:keys [body]}]
  (let [{:keys [chat_id message_id]} body]
    (service/pin-message chat_id message_id)
    {:status 200 :body {:message "Message pinned"}}))

(defn unpin-message-handler [{:keys [body]}]
  (let [{:keys [chat_id message_id]} body]
    (service/unpin-message chat_id message_id)
    {:status 200 :body {:message "Message unpinned"}}))

(defn add-message-handler [{:keys [body]}]
  (let [{:keys [chat_id sender_id message_text type file_url file_name file_size file_mime forwarded_from reply_to]} body]
    {:status 201 :body {:message-id (service/add-message chat_id sender_id message_text
                                                         :type (keyword (or type "text"))
                                                         :file-url file_url
                                                         :file-name file_name
                                                         :file-size file_size
                                                         :file-mime file_mime
                                                         :forwarded-from forwarded_from
                                                         :reply-to reply_to)}}))

(defn forward-message-handler [{:keys [body]}]
  (let [{:keys [chat_id sender_id orig_message_id]} body]
    {:status 201 :body {:message-id (service/forward-message chat_id sender_id orig_message_id)}}))

(defn reply-message-handler [{:keys [body]}]
  (let [{:keys [chat_id sender_id message_text reply_to]} body]
    {:status 201 :body {:message-id (service/reply-message chat_id sender_id message_text reply_to)}}))

(defn delete-message-handler [{:keys [body]}]
  (let [{:keys [message_id]} body]
    (service/delete-message message_id)
    {:status 200 :body {:message "Message deleted"}}))

(defn get-messages-for-chat-handler [{{:keys [chat_id]} :path-params}]
  {:status 200 :body {:messages (service/get-messages-for-chat chat_id)}})

(defn unread-counts-handler [{{:keys [user_id]} :path-params}]
  {:status 200 :body {:unread-counts (service/unread-counts user_id)}})

(defn mark-chat-read-handler [{:keys [body]}]
  (let [{:keys [chat_id user_id]} body]
    (service/mark-chat-read chat_id user_id)
    {:status 200 :body {:message "All messages marked as read"}}))