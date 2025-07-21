(ns marketplace-shum.chats.routes
  (:require [marketplace-shum.chats.handler :as handler]
            [clojure.spec.alpha :as s]))

(s/def ::user-id string?)
(s/def ::chat-id string?)
(s/def ::user-ids (s/coll-of string? :kind vector?))
(s/def ::name string?)
(s/def ::avatar-url string?)
(s/def ::message-id string?)
(s/def ::sender-id string?)
(s/def ::message-text string?)
(s/def ::type string?)
(s/def ::file-url string?)
(s/def ::file-name string?)
(s/def ::file-size int?)
(s/def ::file-mime string?)
(s/def ::forwarded-from string?)
(s/def ::reply-to string?)

(def routes
  ["/chats"
   {:tags ["chats"]}

   [""
    {:get {:summary "Get chats for user"
           :description "Returns a list of chats for the specified user."
           :parameters {:query {:user-id ::user-id}}
           :responses {200 {:body {:chats vector?}}
                       401 {:body {:error string?}}}
           :handler #'handler/get-chats-for-user-handler}

     :post {:summary "Create chat"
            :description "Creates a new chat with the given users, optional name and avatar."
            :parameters {:body {:user_ids ::user-ids
                                :name (s/nilable ::name)
                                :avatar_url (s/nilable ::avatar-url)}}
            :responses {201 {:body {:chat-id string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/create-chat-handler}}]

   ["/add_user"
    {:post {:summary "Add user to chat"
            :description "Adds a user to an existing chat."
            :parameters {:body {:chat_id ::chat-id
                                :user_id ::user-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/add-user-to-chat-handler}}]

   ["/remove_user"
    {:post {:summary "Remove user from chat"
            :description "Removes a user from a chat."
            :parameters {:body {:chat_id ::chat-id
                                :user_id ::user-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/remove-user-from-chat-handler}}]

   ["/pin_message"
    {:post {:summary "Pin message"
            :description "Pins a message in the chat."
            :parameters {:body {:chat_id ::chat-id
                                :message_id ::message-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/pin-message-handler}}]

   ["/unpin_message"
    {:post {:summary "Unpin message"
            :description "Unpins a message in the chat."
            :parameters {:body {:chat_id ::chat-id
                                :message_id ::message-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/unpin-message-handler}}]

   ["/messages"
    {:get {:summary "Get messages for chat"
           :description "Returns all messages for the specified chat."
           :parameters {:query {:chat_id ::chat-id}}
           :responses {200 {:body {:messages vector?}}
                       400 {:body {:error string?}}}
           :handler #'handler/get-messages-for-chat-handler}

     :post {:summary "Send message"
            :description "Sends a message (text or file) to the chat."
            :parameters {:body {:chat_id ::chat-id
                                :sender_id ::sender-id
                                :message_text ::message-text
                                :type (s/nilable ::type)
                                :file_url (s/nilable ::file-url)
                                :file_name (s/nilable ::file-name)
                                :file_size (s/nilable ::file-size)
                                :file_mime (s/nilable ::file-mime)
                                :forwarded_from (s/nilable ::forwarded-from)
                                :reply_to (s/nilable ::reply-to)}}
            :responses {201 {:body {:message-id string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/add-message-handler}}]

   ["/messages/forward"
    {:post {:summary "Forward message"
            :description "Forwards an existing message to another chat."
            :parameters {:body {:chat_id ::chat-id
                                :sender_id ::sender-id
                                :orig_message_id ::message-id}}
            :responses {201 {:body {:message-id string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/forward-message-handler}}]

   ["/messages/reply"
    {:post {:summary "Reply to message"
            :description "Sends a reply to a specific message in a chat."
            :parameters {:body {:chat_id ::chat-id
                                :sender_id ::sender-id
                                :message_text ::message-text
                                :reply_to ::message-id}}
            :responses {201 {:body {:message-id string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/reply-message-handler}}]

   ["/messages/delete"
    {:post {:summary "Delete message"
            :description "Marks a message as deleted."
            :parameters {:body {:message_id ::message-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/delete-message-handler}}]

   ["/messages/unread_count"
    {:get {:summary "Get unread message counts"
           :description "Returns the count of unread messages per chat for the user."
           :parameters {:query {:user_id ::user-id}}
           :responses {200 {:body {:unread-counts vector?}}
                       400 {:body {:error string?}}}
           :handler #'handler/unread-counts-handler}}]

   ["/messages/mark_read"
    {:post {:summary "Mark all messages as read"
            :description "Marks all messages in a chat as read for the user."
            :parameters {:body {:chat_id ::chat-id
                                :user_id ::user-id}}
            :responses {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
            :handler #'handler/mark-chat-read-handler}}]
  ])