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

(defn add-role-to-user! [conn user-id role-uuid]
  (let [user-id (if (uuid? user-id) user-id (java.util.UUID/fromString user-id))
        role-eid (ffirst (d/q '[:find ?e
                                :in $ ?role-id
                                :where [?e :role/id ?role-id]]
                              (d/db conn) role-uuid))]
    (when role-eid
      (d/transact conn {:tx-data [[:db/add [:user/id user-id] :user/roles role-eid]]}))))

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

(defn get-favorite-categories [conn user-id]
  (as-> (d/db conn) $ 
      (d/pull $
          [:user/favorite-categories] 
          [:user/id user-id])
      (:user/favorite-categories $)
      (map :db/ident $)))

(defn remove-favorite-category [conn user-id categories]
  (d/transact conn {:tx-data (mapv #(vector :db/retract [:user/id user-id] :user/favorite-categories %) categories)}))

(defn set-favorite-category [conn user-id categories]
  (d/transact conn {:tx-data (mapv #(vector :db/add [:user/id user-id] :user/favorite-categories %) categories)}))

(defn update-favorite-categories! [conn user-id favorite-categories]
  (let [user-eid (if (uuid? user-id) user-id (java.util.UUID/fromString user-id))
        old-categories (get-favorite-categories conn user-eid)]
    
    (when (seq old-categories)
      (remove-favorite-category conn user-eid old-categories))

    (when (seq favorite-categories)
      (set-favorite-category conn user-eid favorite-categories))))


(comment 
  
  (require '[marketplace-shum.infra.db :refer [db]])
  
  (def user-id (java.util.UUID/fromString "fd8f2189-1a48-45c9-bfea-1836c60b1a00"))
  
  (def old-categories (get-favorite-categories db user-id))

  (remove-favorite-category db user-id old-categories)

  (set-favorite-category db user-id [:ad-category/shoes])

  (update-favorite-categories! db user-id [:ad-category/shoes])
  
  )