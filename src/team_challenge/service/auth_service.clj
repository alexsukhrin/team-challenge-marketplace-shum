(ns team-challenge.service.auth-service
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [team-challenge.config :as config]
            [team-challenge.db :as db]
            [datomic.client.api :as d]
            [clj-time.core :as t]
            [team-challenge.repository.auth-repository :as auth-repo]))

(defn secret []
  (:jwt-secret config/*config*))

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
(defn- create-token [claims] (jwt/sign claims (secret)))

(defn create-access-token
  "Creates a short-lived access token."
  [user]
  (let [jti (str (java.util.UUID/randomUUID))]
    (create-token {:type :access
                   :user-id (:user/id user)
                   :jti jti
                   :exp (t/plus (t/now) access-token-lifetime)})))

(defn create-refresh-token
  "Creates a long-lived refresh token."
  [user]
  (let [jti (java.util.UUID/randomUUID)
        token (create-token {:type :refresh
                             :user-id (:user/id user)
                             :jti jti
                             :exp (t/plus (t/now) refresh-token-lifetime)})]
    (d/transact db/conn {:tx-data [{:auth/refresh-token (str jti)}]})
    token))

;;; Token Verification
(defn- verify-token [token]
  (try
    (jwt/unsign token (secret))
    (catch Exception _ nil)))

(defn verify-access-token
  "Verifies an access token and checks if it's blacklisted."
  [token]
  (when-let [claims (verify-token token)]
    (let [jti (:jti claims)]
      (when (some? jti)
        (when-not (auth-repo/is-token-blacklisted? jti)
          claims)))))

(defn verify-refresh-token
  "Verifies a refresh token and checks if it has been revoked."
  [token]
  (when-let [claims (verify-token token)]
    (let [jti (:jti claims)]
      (when (some? jti)
        (when-not (auth-repo/is-refresh-token-revoked? jti)
          claims)))))

;;; Token Revocation
(defn blacklist-token!
  "Adds an access token's JTI to the blacklist."
  [jti]
  (auth-repo/add-token-to-blacklist! jti))

(defn revoke-refresh-token!
  "Adds a refresh token's JTI to the revoked list."
  [jti]
  (auth-repo/add-refresh-token-to-revoked-list! jti))

(comment

  (verify-password "password1986" "bcrypt+sha512$14262d9e4dc8f53a98334a43abe2de30$12$926c63920d43b630e9245cbd39fdb7fad94160fcc6dc6b01"))