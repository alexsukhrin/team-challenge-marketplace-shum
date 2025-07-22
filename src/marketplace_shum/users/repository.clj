(ns marketplace-shum.users.repository
  (:require
   [datomic.client.api :as d]
   [clj-time.core :as t]))

(defn generate-expiry []
  (java.util.Date. (.getMillis (t/plus (t/now) (t/days 1)))))

(defn create-user! [conn {:keys [email password first-name last-name]}]
  (let [user-id (random-uuid)
        email-confirmation-token (random-uuid)
        tx {:tx-data [{:user/id                                  user-id
                       :user/email                               email
                       :user/password                            password
                       :user/first-name                          first-name
                       :user/last-name                           last-name
                       :user/email-confirmed?                    false
                       :user/email-confirmation-token            email-confirmation-token
                       :user/email-confirmation-token-expires-at (generate-expiry)
                       :user/created-at                          (java.util.Date.)}]}
        tx-result (d/transact conn tx)]
    {:user-id user-id
     :email-confirmation-token email-confirmation-token
     :tx-result tx-result}))

(defn find-user-by-id
  [conn id]
  (d/pull (d/db conn) '[*] [:user/id (if (uuid? id) id (java.util.UUID/fromString id))]))

(defn user-role-names [conn user]
  (let [db (d/db conn)
        role-ids (:user/roles user)
        roles (map #(d/pull db [:role/name] (:db/id %)) role-ids)]
    (map :role/name roles)))

(defn find-user-by-attr
  [conn attr value]
  (let [result (d/q '[:find ?e
                      :in $ ?attr ?value
                      :where [?e ?attr ?value]]
                    (d/db conn) attr value)
        eid (ffirst result)]
    (when eid
      (d/pull (d/db conn) '[*] eid))))

(defn find-user-by-email [conn email]
  (find-user-by-attr conn :user/email email))

(defn find-user-by-email-confirmation-token [conn token]
  (find-user-by-attr conn :user/email-confirmation-token (if (uuid? token) token (java.util.UUID/fromString token))))

(defn set-email-confirmed! [conn id confirmed?]
  (d/transact conn {:tx-data [[:db/add [:user/id (if (uuid? id) id (java.util.UUID/fromString id))] :user/email-confirmed? confirmed?]]}))

(defn find-role-uuid-by-name [conn role-name]
  (ffirst (d/q '[:find ?id
                 :in $ ?name
                 :where
                 [?e :role/name ?name]
                 [?e :role/id ?id]]
               (d/db conn) role-name)))

(defn set-refresh-token! [conn id refresh-token]
  (d/transact conn {:tx-data [[:db/add [:user/id (if (uuid? id) id (java.util.UUID/fromString id))]
                               :user/refresh-token (if (uuid? refresh-token) refresh-token (java.util.UUID/fromString refresh-token))]]}))

(defn update-confirmation-token! [conn user-id]
  (let [token (random-uuid)
        expiry (generate-expiry)]
    (d/transact conn {:tx-data [{:db/id user-id
                                 :user/email-confirmation-token token
                                 :user/email-confirmation-token-expires-at expiry}]})
    {:email-confirmation-token token}))

(defn get-attributes [conn user-id attr-key]
  (as-> (d/db conn) $
    (d/pull $ [attr-key] [:user/id user-id])
    (get $ attr-key)
    (map :db/ident $)))

(defn remove-attributes [conn user-id attr-key values]
  (d/transact conn {:tx-data (mapv #(vector :db/retract [:user/id user-id] attr-key %) values)}))

(defn set-attributes [conn user-id attr-key values]
  (d/transact conn {:tx-data (mapv #(vector :db/add [:user/id user-id] attr-key %) values)}))

(defn update-attributes! [conn user-id attr-key new-values]
  (let [user-eid (if (uuid? user-id) user-id (java.util.UUID/fromString user-id))
        old-values (get-attributes conn user-eid attr-key)]

    (when (seq old-values)
      (remove-attributes conn user-eid attr-key old-values))

    (when (seq new-values)
      (set-attributes conn user-eid attr-key new-values))))

(comment

  (require '[marketplace-shum.infra.db :refer [db]])

  (find-user-by-email db "alexandrvirtual@gmail.com")

  (find-user-by-email db "rashiki44@gmail.com"))