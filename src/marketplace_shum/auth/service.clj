(ns marketplace-shum.auth.service
  (:require
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as t]
   [marketplace-shum.infra.config :as config]
   [marketplace-shum.infra.db :refer [db]]
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.auth.repository :as auth-repo]))

(defn secret []
  (:jwt-secret config/*config*))

(defn hash-password
  "Hashes a password using bcrypt."
  [password]
  (hashers/derive password))

(defn verify-password
  "Checks if a password matches its hash."
  [password-to-check hashed-password]
  (hashers/check password-to-check hashed-password))

(defn- create-token [claims]
  (jwt/sign claims (secret) {:alg :hs256}))

(defn create-token-for-user
  [{:keys [user-id type lifetime]}]
  (let [jti (str (java.util.UUID/randomUUID))
        user (user-repo/find-user-by-id db user-id)
        roles (user-repo/user-role-names db user)
        token (create-token {:type type
                             :user-id user-id
                             :roles roles
                             :jti jti
                             :exp (t/plus (t/now) lifetime)})]
    (when (= type :refresh)
      (user-repo/set-refresh-token! db user-id jti))
    token))

(defn create-access-token
  "Creates a short-lived access token."
  [user-id]
  (create-token-for-user {:user-id user-id
                          :type :access
                          :lifetime (t/minutes 15)}))

(defn create-refresh-token
  "Creates a long-lived refresh token."
  [user-id]
  (create-token-for-user {:user-id user-id
                          :type :refresh
                          :lifetime (t/days 7)}))

(defn verify-token-of-type [token expected-type]
  (let [claims (jwt/unsign token (secret) {:alg :hs256})]
    (when (and (= (-> claims :type keyword) expected-type)
               (> (:exp claims) (.getEpochSecond (java.time.Instant/now))))
      claims)))

(defn verify-access-token [token]
  (verify-token-of-type token :access))

(defn verify-refresh-token [token]
  (verify-token-of-type token :refresh))

(defn confirm-user! [db user-id]
  (user-repo/set-email-confirmed! db user-id true)
  (user-repo/set-attributes db user-id :user/roles [:role/user]))

(defn generate-otp []
  (format "%06d" (rand-int 1000000)))

(defn set-otp! [user-id otp]
  (auth-repo/set-otp! db user-id otp))
