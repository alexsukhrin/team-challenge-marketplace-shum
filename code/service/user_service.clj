(ns team-challenge.service.user-service
  (:require [team-challenge.repository.user-repository :as user-repo]
            [team-challenge.service.auth-service :as auth-service]
            [team-challenge.service.email-service :as email-service]
            [clj-time.core :as t]
            [team-challenge.domain.user :as domain-user]))

(def dummy-hash
  "$2a$10$7EqJtq98hPqEX7fNZaFWoO5e5p8Y8QZC1Z2ZQFQFQFQFQFQFQFQFQ") ; bcrypt for "password"

(defn- create-token-pair [user]
  {:access-token (auth-service/create-access-token user)
   :refresh-token (auth-service/create-refresh-token user)})

(defn register-user
  "Creates a new user and sends a confirmation email."
  [user-data]
  (if (user-repo/get-user-by-email (:email user-data))
    (throw (ex-info "Email already exists" {:type :email-conflict
                                            :email (:email user-data)}))
    (let [hashed-password (auth-service/hash-password (:password user-data))
          user (user-repo/create-user! (assoc user-data :password hashed-password))
          confirmation-token (str (java.util.UUID/randomUUID))
          expires-at (java.util.Date. (.getMillis (t/plus (t/now) (t/days 1))))
          user-name (domain-user/full-name {:first-name (:users/first_name user)
                                            :last-name (:users/last_name user)})]
      (user-repo/set-confirmation-token! (:users/id user) confirmation-token expires-at)
      (email-service/send-confirmation-email (:users/email user) confirmation-token user-name)
      user)))

(defn login
  "Verifies credentials and returns a token pair if they are valid."
  [email password]
  (let [user (user-repo/get-user-by-email email)
        password-hash (cond
                        (and user (:users/email_confirmed user)) (:users/password user)
                        :else dummy-hash)]
    (when (and user
               (:users/email_confirmed user)
               (auth-service/verify-password password password-hash))
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
          user {:user/id user-id}] ; Only ID is needed to create a new token
      (auth-service/revoke-refresh-token! (:jti claims))
      (create-token-pair user))))

(defn request-password-reset
  "Generates and saves a password reset token."
  [email]
  (when-let [user (user-repo/get-user-by-email email)]
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
    (let [expires-at (:users/email_confirmation_token_expires_at user)
          expires-at-joda (if (instance? java.util.Date expires-at)
                            (org.joda.time.DateTime. ^java.util.Date expires-at)
                            expires-at)]
      (when (t/before? (t/now) expires-at-joda)
        (user-repo/confirm-user-email! (:users/id user))
        true))))

(comment

  (def user (register-user {:first_name "alexandr"
                            :last_name "sukhryn"
                            :email "alexandrvirtual@gmail.com"
                            :password "password1986"}))

  (:users/id user)

  (def confirmation-token (str (java.util.UUID/randomUUID)))
  (def expires-at (java.util.Date. (.getMillis (t/plus (t/now) (t/days 1)))))
  (def confirmation-token (user-repo/set-confirmation-token! (:user/id user) confirmation-token expires-at))
  (def confirmation-email (email-service/send-confirmation-email (:user/email user) confirmation-token (str (:user-profile/first-name user) " " (:user-profile/last-name user))))

  (login "alexandrvirtual@gmail.com" "password1986")

  (confirm-email "57864cd3-1093-45e1-b5cc-c94577fdef0b"))