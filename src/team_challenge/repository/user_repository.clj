(ns team-challenge.repository.user-repository
  (:require [datomic.client.api :as d]
            [team-challenge.db :as db]
            [team-challenge.service.auth-service :as auth-service]))

(defn create-user! [{:keys [email password first_name last_name]}]
  (let [user-id (java.util.UUID/randomUUID)
        user-tempid -1000001 ; tempid для Datomic Client API
        profile-id (java.util.UUID/randomUUID)
        hashed-password (auth-service/hash-password password)]
    (d/transact db/conn
      {:tx-data [{:db/id user-tempid
                  :user/id user-id
                  :user/email email
                  :user/email-confirmed? false
                  :auth/password-hash hashed-password
                  :user/created-at (java.util.Date.)}
                 {:user-profile/id profile-id
                  :user-profile/user user-tempid
                  :user-profile/first-name first_name
                  :user-profile/last-name last_name}]})
    {:user/id user-id
     :user/email email
     :user-profile/id profile-id
     :user-profile/first-name first_name
     :user-profile/last-name last_name}))

(defn find-user-by-email [email]
  (let [db (d/db db/conn)
        eid (ffirst
              (d/q '[:find ?e
                     :in $ ?email
                     :where [?e :user/email ?email]]
                   db email))]
    (when eid
      (d/pull db
              '[*
                {:user-profile/_user [:user-profile/id
                                      :user-profile/first-name
                                      :user-profile/last-name]}]
              eid))))

(defn set-password-reset-token! [user-id token expires-at]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :auth/password-reset-token token
                           :auth/password-reset-token-expires-at expires-at}]}))

(defn set-confirmation-token! [user-id token expires-at]
  (d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :user/email-confirmation-token token
                           :user/email-confirmation-token-expires-at expires-at}]}))

(defn find-user-by-reset-token [token]
  (let [db (d/db db/conn)]
    (when-let [user-eid (d/q '[:find ?e .
                               :in $ ?token
                               :where [?e :auth/password-reset-token ?token]]
                             db token)]
      (d/pull db '[*] user-eid))))

(defn find-user-by-confirmation-token [token]
  (let [db (d/db db/conn)
        result (d/q '[:find ?e
                      :in $ ?token
                      :where [?e :user/email-confirmation-token ?token]]
                    db token)
        user-eid (ffirst result)]
    (when user-eid
      (d/pull db '[*] user-eid))))

(defn update-password! [user-id new-password-hash]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :auth/password-hash new-password-hash
                           :auth/password-reset-token nil
                           :auth/password-reset-token-expires-at nil}]}))

(defn confirm-user-email! [user-id]
  (let [db (d/db db/conn)
        eid (ffirst (d/q '[:find ?e :in $ ?user-id :where [?e :user/id ?user-id]] db user-id))
        user (d/pull db '[ :user/email-confirmation-token :user/email-confirmation-token-expires-at ] eid)
        token (:user/email-confirmation-token user)
        expires-at (:user/email-confirmation-token-expires-at user)]
    (d/transact db/conn
      {:tx-data (cond-> [{:db/id [:user/id user-id]
                          :user/email-confirmed? true}]
                 token (conj [:db/retract [:user/id user-id] :user/email-confirmation-token token])
                 expires-at (conj [:db/retract [:user/id user-id] :user/email-confirmation-token-expires-at expires-at]))})))


(comment

  (create-user! {:first_name "alexandr"
                 :last_name "sukhryn"
                 :email "alexandrvirtual@gmail.com"
                 :password "password1986"})

  (d/q '[:find ?a
         :where [?a :db/ident :user/email]
         [?a :db/unique ?u]]
       (d/db db/conn))

  (d/q '[:find ?e
         :where [?e :user/email "alexandrvirtual@gmail.com"]]
       (d/db db/conn))

  (d/q '[:find ?e :where [?e :user/email]] (d/db db/conn))

  (d/entity (d/db db/conn) :db/doc)

  (d/q '[:find (count ?e) . :where [?e :user/email]] (d/db db/conn))

  (d/list-databases db/client {})

  (def db (d/db db/conn))

  (def user-tempid -1000001)
  (def user-id (java.util.UUID/randomUUID))
  (def profile-id (java.util.UUID/randomUUID))
  (def first_name "Alexandr")
  (def last_name "Sukhryn")
  (def email "alexandrvirtual@gmail.com")
  (def hashed-password (auth-service/hash-password "password"))


  (d/transact db/conn {:tx-data [{:db/id user-tempid
                                  :user/id user-id
                                  :user/email email
                                  :user/email-confirmed? false
                                  :auth/password-hash hashed-password
                                  :user/created-at (java.util.Date.)}
                                 {:user-profile/id profile-id
                                  :user-profile/user user-tempid
                                  :user-profile/first-name first_name
                                  :user-profile/last-name last_name}]})

  (d/q '[:find ?e ?user-id ?confirmed
         :in $ ?email
         :where
         [?e :user/email ?email]
         [?e :user/id ?user-id]
         [?e :user/email-confirmed? ?confirmed]]
       (d/db db/conn) email)

  (let [db (d/db db/conn)
        eid (ffirst (d/q '[:find ?e
                           :in $ ?email
                           :where [?e :user/email ?email]]
                         db email))]
    (when eid
      (d/pull db '[*] eid)))
  
  (find-user-by-confirmation-token "c9081132-9937-4f99-ba2b-2afa8242af63")

  (def token "c9081132-9937-4f99-ba2b-2afa8242af63")

  (d/q '[:find ?e . 
         :in $ ?token 
         :where [?e :user/email-confirmation-token ?token]] 
       db token)
  
  (d/q '[:find ?e
       :in $ ?token
       :where
       [?e :user/email-confirmation-token ?token]]
     db token)
  
  (find-user-by-email email)

  )