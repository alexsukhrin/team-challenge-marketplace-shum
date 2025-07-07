(ns marketplace-shum.users.repository
  (:require
   [datomic.client.api :as d]
   [marketplace-shum.infra.db :refer [db]]
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

(defn find-user-by-email
  [conn email]
  (let [result (d/q '[:find ?e
                      :in $ ?email
                      :where [?e :user/email ?email]]
                    (d/db conn) email)
        eid (ffirst result)]
    (when eid
      (d/pull (d/db conn) '[*] eid))))

(defn set-email-confirmed!
  [conn id confirmed?]
  (d/transact conn {:tx-data [[:db/add [:user/id (if (uuid? id) id (java.util.UUID/fromString id))] :user/email-confirmed? confirmed?]]}))

(defn update-confirmation-token! [conn user-id]
  (let [token (random-uuid)
        expiry (generate-expiry)]
    (d/transact conn {:tx-data [{:db/id user-id
                                 :user/email-confirmation-token token
                                 :user/email-confirmation-token-expires-at expiry}]})
    {:email-confirmation-token token}))

(comment

  (d/q '[:find ?e ?ident ?value-type ?value-type-name
         :where [?e :db/ident ?ident]
         [?e :db/valueType ?value-type]
         [?value-type :db/ident ?value-type-name]
         [(namespace ?ident) ?ns]
         [(= ?ns "user")]]
       (d/db db))

  (d/transact db [[:db/retractEntity :user/email-confirmation-token]])

  (d/transact db [{:db/ident :user/email-confirmation-token
                   :db/valueType :db.type/uuid
                   :db/cardinality :db.cardinality/one
                   :db/doc "A token sent to the user to confirm their email address."}])

  (def user-result (create-user! db {:email "alexandrvirtual@gmail.com"
                                     :password "password123"
                                     :first-name "Alexandr"
                                     :last-name "Sukhryn"})))
