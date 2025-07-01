(ns team-challenge.repository.user-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [insert-into columns values select from where delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn create-user! [{:keys [email password first_name last_name]}]
  (let [query (-> (insert-into :users)
                  (columns :email :password :first_name :last_name)
                  (values [[email password first_name last_name]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn get-user-by-id [id]
  (let [query (-> (select :*)
                  (from :users)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn get-user-by-email [email]
  (let [query (-> (select :*)
                  (from :users)
                  (where [:= :email email])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn update-user! [id updates]
  (let [query (-> (h/update :users)
                  (h/set updates)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn delete-user! [id]
  (let [query (-> (delete-from :users)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn set-confirmation-token! [user-id token expires-at]
  (let [ts (when expires-at (java.sql.Timestamp. (.getTime expires-at)))
        query (-> (h/update :users)
                  (h/set {:email_confirmation_token token
                          :email_confirmation_token_expires_at ts})
                  (h/where [:= :id user-id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn set-password-reset-token!
  "Updates the password reset token and its expiration time for the user."
  [user-id token expires-at]
  (let [ts (when expires-at (java.sql.Timestamp. (.getTime expires-at)))
        query (-> (h/update :users)
                  (h/set {:password_reset_token token
                          :password_reset_token_expires_at ts})
                  (h/where [:= :id user-id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn find-user-by-reset-token
  "Returns a user by password reset token."
  [token]
  (let [query (-> (h/select :*)
                  (h/from :users)
                  (h/where [:= :password_reset_token token])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn update-password!
  "Updates the user's password by user-id."
  [user-id new-password-hash]
  (let [query (-> (h/update :users)
                  (h/set {:password new-password-hash
                          :password_reset_token nil
                          :password_reset_token_expires_at nil})
                  (h/where [:= :id user-id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn find-user-by-confirmation-token [token]
  (let [query (-> (h/select :*)
                  (h/from :users)
                  (h/where [:= :email_confirmation_token token])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn confirm-user-email! [user-id]
  (let [query (-> (h/update :users)
                  (h/set {:email_confirmed true
                          :email_confirmation_token nil
                          :email_confirmation_token_expires_at nil
                          :updated_at (java.time.LocalDateTime/now)})
                  (h/where [:= :id user-id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(comment
  (require '[clj-time.core :as t])

  (set-confirmation-token!
   (java.util.UUID/fromString "6a18114b-5164-4f39-a42c-5c1c50a57bf6")
   "token"
   (java.util.Date. (.getMillis (t/plus (t/now) (t/days 1)))))

  (find-user-by-confirmation-token "57864cd3-1093-45e1-b5cc-c94577fdef0b"))