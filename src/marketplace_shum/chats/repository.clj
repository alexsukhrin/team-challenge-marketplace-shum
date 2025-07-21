(ns marketplace-shum.chats.repository
  (:require [datomic.client.api :as d]
            [clj-time.core :as t]))

(defn create-chat! [conn user-ids & {:keys [name avatar-url]}]
  (let [chat-id (random-uuid)
        now (java.util.Date.)
        tx {:tx-data [{:chat/id chat-id
                       :chat/created-at now
                       :chat/updated-at now
                       :chat/participants (mapv #(vector :user/id %) user-ids)
                       :chat/name name
                       :chat/avatar-url avatar-url}]}]
    (d/transact conn tx)
    chat-id))

(defn add-user-to-chat! [conn chat-id user-id]
  (d/transact conn {:tx-data [[:db/add [:chat/id chat-id] :chat/participants [:user/id user-id]]]}))

(defn remove-user-from-chat! [conn chat-id user-id]
  (d/transact conn {:tx-data [[:db/retract [:chat/id chat-id] :chat/participants [:user/id user-id]]]}))

(defn pin-message! [conn chat-id message-id]
  (d/transact conn {:tx-data [[:db/add [:chat/id chat-id] :chat/pinned-messages [:message/id message-id]]]}))

(defn unpin-message! [conn chat-id message-id]
  (d/transact conn {:tx-data [[:db/retract [:chat/id chat-id] :chat/pinned-messages [:message/id message-id]]]}))

(defn add-message! [conn chat-id sender-id text & {:keys [type file-url file-name file-size file-mime forwarded-from reply-to]}]
  (let [msg-id (random-uuid)
        now (java.util.Date.)
        tx {:tx-data [(merge
                        {:message/id msg-id
                         :message/chat [:chat/id chat-id]
                         :message/sender [:user/id sender-id]
                         :message/text text
                         :message/sent-at now
                         :message/type (or type :text)
                         :message/read-by []}
                        (when file-url {:message/file-url file-url})
                        (when file-name {:message/file-name file-name})
                        (when file-size {:message/file-size file-size})
                        (when file-mime {:message/file-mime file-mime})
                        (when forwarded-from {:message/forwarded-from [:message/id forwarded-from]})
                        (when reply-to {:message/reply-to [:message/id reply-to]}))
                      [:db/add [:chat/id chat-id] :chat/updated-at now]]}]
    (d/transact conn tx)
    msg-id))

(defn forward-message! [conn chat-id sender-id orig-message-id]
  (let [db (d/db conn)
        orig-msg (d/pull db '[*] [:message/id orig-message-id])]
    (add-message! conn chat-id sender-id (:message/text orig-msg)
                  :type (:message/type orig-msg)
                  :file-url (:message/file-url orig-msg)
                  :file-name (:message/file-name orig-msg)
                  :file-size (:message/file-size orig-msg)
                  :file-mime (:message/file-mime orig-msg)
                  :forwarded-from orig-message-id)))

(defn reply-message! [conn chat-id sender-id text reply-to-id & opts]
  (apply add-message! conn chat-id sender-id text (concat opts [:reply-to reply-to-id])))

(defn delete-message! [conn message-id]
  (d/transact conn {:tx-data [[:db/add [:message/id message-id] :message/is-deleted true]]}))

(defn get-chats-for-user [conn user-id]
  (let [db (d/db conn)]
    (d/q '[:find (pull ?c [* {:chat/participants [:user/id]}])
           :in $ ?user-id
           :where
           [?c :chat/participants ?u]
           [?u :user/id ?user-id]]
         db user-id)))

(defn get-messages-for-chat [conn chat-id]
  (let [db (d/db conn)]
    (d/q '[:find (pull ?m [* {:message/sender [:user/id :user/first-name :user/last-name]}])
           :in $ ?chat-id
           :where
           [?m :message/chat ?c]
           [?c :chat/id ?chat-id]]
         db chat-id)))

(defn unread-counts [conn user-id]
  (let [db (d/db conn)]
    (d/q '[:find ?chat-id (count ?m)
           :in $ ?user-id
           :where
           [?c :chat/participants ?u]
           [?u :user/id ?user-id]
           [?m :message/chat ?c]
           (not [?m :message/read-by ?u])
           [?c :chat/id ?chat-id]]
         db user-id)))

(defn mark-chat-read! [conn chat-id user-id]
  (let [db (d/db conn)
        user-eid (ffirst (d/q '[:find ?u :in $ ?user-id :where [?u :user/id ?user-id]] db user-id))
        msg-ids (map first (d/q '[:find ?m :in $ ?chat-id :where [?m :message/chat ?c] [?c :chat/id ?chat-id]] db chat-id))]
    (d/transact conn {:tx-data (mapv #(vector :db/add % :message/read-by user-eid) msg-ids)}))) 


(comment

  (require '[marketplace-shum.infra.db :refer [db]])

  (def user-id (java.util.UUID/fromString "fd8f2189-1a48-45c9-bfea-1836c60b1a00"))
  (def user-id-2 (java.util.UUID/fromString "231eb410-0a32-4714-995f-e569a115e5d5"))

  (get-chats-for-user db user-id)

  (create-chat! db [user-id user-id-2]
                {:name "chat-gpt" 
                 :avatar-url "http://localhost:3000/category/shoes.jpg"})

  )