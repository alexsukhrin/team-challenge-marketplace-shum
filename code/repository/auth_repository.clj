(ns team-challenge.repository.auth-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn is-token-blacklisted?
  "Checks if the given JTI is blacklisted."
  [jti]
  (let [query (-> (select :token)
                  (from :blacklisted_tokens)
                  (where [:= :token jti])
                  sql/format)]
    (some? (jdbc/execute-one! datasource query))))

(defn is-refresh-token-revoked?
  "Checks if the given JTI is in the revoked refresh token list."
  [jti]
  (let [query (-> (select :token)
                  (from :refresh_tokens)
                  (where [:= :token jti])
                  sql/format)]
    (not (some? (jdbc/execute-one! datasource query)))))

(defn add-token-to-blacklist!
  "Adds the given JTI to the blacklist."
  [jti]
  (let [query (-> (insert-into :blacklisted_tokens)
                  (columns :token)
                  (values [[jti]])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn add-refresh-token!
  "Adds a refresh token for the user."
  [user-id jti]
  (let [query (-> (insert-into :refresh_tokens)
                  (columns :token :user_id)
                  (values [[jti user-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn revoke-refresh-token!
  "Removes the given JTI from the valid refresh token list (revokes it)."
  [jti]
  (let [query (-> (delete-from :refresh_tokens)
                  (where [:= :token jti])
                  sql/format)]
    (jdbc/execute-one! datasource query)))
