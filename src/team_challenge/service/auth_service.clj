(ns team-challenge.service.auth-service
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [team-challenge.config :as config]
            [team-challenge.db :as db]
            [datomic.client.api :as d]
            [clj-time.core :as t]
            [team-challenge.repository.auth-repository :as auth-repo]))

(def ^:private secret (:jwt-secret @config/*config*))
(def ^:private access-token-lifetime (t/minutes 15))
(def ^:private refresh-token-lifetime (t/days 7))

;;; Password Hashing
(defn hash-password
  "Hashes a password using bcrypt."
  [password]
  (hashers/derive password))

(defn verify-password
  "Checks if a password matches its hash."
  [password-to-check hashed-password]
  (hashers/check password-to-check hashed-password))

;;; Token Creation
(defn- create-token [claims] (jwt/sign claims secret))

(defn create-access-token
  "Creates a short-lived access token."
  [user]
  (create-token {:type :access
                 :user-id (:user/id user)
                 :exp (t/plus (t/now) access-token-lifetime)}))

(defn create-refresh-token
  "Creates a long-lived refresh token."
  [user]
  (let [jti (java.util.UUID/randomUUID)
        token (create-token {:type :refresh
                             :user-id (:user/id user)
                             :jti jti
                             :exp (t/plus (t/now) refresh-token-lifetime)})]
    @(d/transact db/conn {:tx-data [{:auth/refresh-token (str jti)}]})
    token))

;;; Token Verification
(defn- verify-token [token]
  (try
    (jwt/unsign token secret)
    (catch Exception _ nil)))

(defn verify-access-token
  "Verifies an access token and checks if it's blacklisted."
  [token]
  (when-let [claims (verify-token token)]
    (when-not (auth-repo/is-token-blacklisted? (:jti claims))
      claims)))

(defn verify-refresh-token
  "Verifies a refresh token and checks if it has been revoked."
  [token]
  (when-let [claims (verify-token token)]
    (when-not (auth-repo/is-refresh-token-revoked? (:jti claims))
      claims)))

;;; Token Revocation
(defn blacklist-token!
  "Adds an access token's JTI to the blacklist."
  [jti]
  (auth-repo/add-token-to-blacklist! jti))

(defn revoke-refresh-token!
  "Adds a refresh token's JTI to the revoked list."
  [jti]
  (auth-repo/add-refresh-token-to-revoked-list! jti)) 