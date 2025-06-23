(ns team-challenge.repository.user-repository
  (:require [datomic.client.api :as d]
            [team-challenge.db :as db]
            [team-challenge.service.auth-service :as auth-service]))

(defn create-user [user]
  (let [user-id (java.util.UUID/randomUUID)
        tx-data [(merge
                   {:user/id user-id
                    :user/username (:username user)
                    :user/email (:email user)}
                   (select-keys user [:auth/password-hash]))]]
    @(d/transact db/conn {:tx-data tx-data})
    {:user/id user-id}))

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
  (d/q '[:find (pull ?e [*]) .
         :in $ ?email
         :where [?e :user/email ?email]]
       (d/db db/conn) email))

(defn set-password-reset-token! [user-id token expires-at]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :auth/password-reset-token token
                           :auth/password-reset-token-expires-at expires-at}]}))

(defn set-confirmation-token! [user-id token expires-at]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :user/email-confirmation-token token
                           :user/email-confirmation-token-expires-at expires-at}]}))

(defn find-user-by-reset-token [token]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?token
         :where [?e :auth/password-reset-token ?token]]
       (d/db db/conn) token))

(defn find-user-by-confirmation-token [token]
  (d/q '[:find (pull ?e [*]) .
         :in $ ?token
         :where [?e :user/email-confirmation-token ?token]]
       (d/db db/conn) token))

(defn update-password! [user-id new-password-hash]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :auth/password-hash new-password-hash
                           :auth/password-reset-token nil
                           :auth/password-reset-token-expires-at nil}]}))

(defn confirm-user-email! [user-id]
  @(d/transact db/conn
               {:tx-data [{:db/id [:user/id user-id]
                           :user/email-confirmed? true
                           :user/email-confirmation-token nil
                           :user/email-confirmation-token-expires-at nil}]}))


(comment
  
  (create-user! {:first_name "alexandr" 
                 :last_name "sukhryn" 
                 :email "alexandrvirtual@gmail.com"
                 :password "password1986"})
  
  )