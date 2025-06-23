(ns team-challenge.service.user-service
  (:require [team-challenge.repository.user-repository :as user-repo]
            [team-challenge.service.auth-service :as auth-service]
            [team-challenge.service.email-service :as email-service]
            [clj-time.core :as t]))

(defn- create-token-pair [user]
  {:access-token (auth-service/create-access-token user)
   :refresh-token (auth-service/create-refresh-token user)})

(defn register-user
  "Creates a new user and sends a confirmation email."
  [user-data]
  (let [user (user-repo/create-user! user-data)
        confirmation-token (str (java.util.UUID/randomUUID))
        expires-at (java.util.Date. (.getMillis (t/plus (t/now) (t/days 1))))]
    (user-repo/set-confirmation-token! (:user/id user) confirmation-token expires-at)
    (email-service/send-confirmation-email (:user/email user) confirmation-token)
    user))

(defn login
  "Verifies credentials and returns a token pair if they are valid."
  [email password]
  (when-let [user (user-repo/find-user-by-email email)]
    (when (and (:user/email-confirmed? user)
               (auth-service/verify-password password (:auth/password-hash user)))
      (create-token-pair user))))

(defn logout
  "Adds the provided access token to the blacklist."
  [access-token]
  (when-let [claims (auth-service/verify-access-token access-token)]
    (auth-service/blacklist-token! (:jti claims))))

(defn refresh-tokens
  "Takes a refresh token and returns a new token pair."
  [refresh-token]
  (when-let [claims (auth-service/verify-refresh-token refresh-token)]
    (let [user-id (:user-id claims)
          user {:user/id user-id}] ; Потрібен тільки ID для створення нового токена
      (auth-service/revoke-refresh-token! (:jti claims))
      (create-token-pair user))))

(defn request-password-reset
  "Generates and saves a password reset token."
  [email]
  (when-let [user (user-repo/find-user-by-email email)]
    (let [token (str (java.util.UUID/randomUUID))
          expires-at (java.util.Date. (.getMillis (t/plus (t/now) (t/hours 1))))]
      (user-repo/set-password-reset-token! (:user/id user) token expires-at)
      token)))

(defn reset-password
  "Checks the token and sets a new password."
  [token new-password]
  (when-let [user (user-repo/find-user-by-reset-token token)]
    (let [expires-at (:auth/password-reset-token-expires-at user)]
      (when (t/before? (t/now) expires-at)
        (let [new-hashed-password (auth-service/hash-password new-password)]
          (user-repo/update-password! (:user/id user) new-hashed-password)
          true)))))

(defn confirm-email
  "Confirms a user's email by token."
  [token]
  (when-let [user (user-repo/find-user-by-confirmation-token token)]
    (let [expires-at (:user/email-confirmation-token-expires-at user)]
      (when (t/before? (t/now) expires-at)
        (user-repo/confirm-user-email! (:user/id user))
        true))))

(comment
  
  (register-user {:first_name "alexandr" 
                 :last_name "sukhryn" 
                 :email "alexandrvirtual@gmail.com"
                 :password "password1986"})
  )