(ns marketplace-shum.auth.handler
  (:require
   [marketplace-shum.auth.service :as auth-service]
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.infra.db :refer [db]]
   [marketplace-shum.email.service :as email-service]
   [marketplace-shum.auth.repository :as auth-repo]))

(defn register-handler [{{{:keys [first-name last-name password email]} :body} :parameters}]
  (let [user (user-repo/find-user-by-email db email)]
    (cond
      (nil? user)
      (let [{:keys [email-confirmation-token user-id]}
            (user-repo/create-user! db {:first-name first-name
                                        :last-name last-name
                                        :password (auth-service/hash-password password)
                                        :email email})]
        (email-service/send-confirmation-email email email-confirmation-token first-name)
        {:status 201
         :body {:access-token (auth-service/create-access-token user-id)
                :refresh-token (auth-service/create-refresh-token user-id)}})

      (not (:user/email-confirmed? user))
      (let [{:keys [email-confirmation-token]} (user-repo/update-confirmation-token! db (:db/id user))]

        (email-service/send-confirmation-email email email-confirmation-token (:user/first-name user))
        {:status 200
         :body {:message "Confirmation email re-sent."}})

      :else
      {:status 409
       :body {:error "conflict"
              :message "A user with this email already exists."}})))

(defn login-handler [{{{:keys [email password]} :body} :parameters}]
  (if-let [user (user-repo/find-user-by-email db email)]
    (if (auth-service/verify-password password (:user/password user))
      {:status 200
       :body {:access-token (auth-service/create-access-token (:user/id user))
              :refresh-token (auth-service/create-refresh-token (:user/id user))}}

      {:status 401
       :body {:error "invalid_credentials"
              :message "Invalid password"}})

    {:status 401
     :body {:error "invalid_credentials"
            :message "Invalid email"}}))

(defn confirm-email-handler [{{:keys [token]} :query-params}]
  (if-let [user (user-repo/find-user-by-email-confirmation-token db token)]
    (let [expires-at (:user/email-confirmation-expiry user)
          now (java.util.Date.)
          user-id (:user/id user)]
      (cond
        (:user/email-confirmed? user)
        {:status 200 :body {:message "Email already confirmed"}}

        (and expires-at (.before expires-at now))
        {:status 410 :body {:message "Confirmation token expired"}}

        :else
        (if (auth-service/confirm-user! db user-id)
          {:status 200 :body {:access-token (auth-service/create-access-token user-id)
                              :refresh-token (auth-service/create-refresh-token user-id)}}
          {:status 500 :body {:message "Role 'user' not found. Contact support."}})))
    {:status 404 :body {:message "Token not found"}}))

(defn logout-handler [_]
  {:status 200
   :body {:message "Logged out successfully"}})

(defn refresh-token-handler [{{{:keys [refresh-token]} :body} :parameters}]
  (let [claims (auth-service/verify-refresh-token refresh-token)
        now (.getEpochSecond (java.time.Instant/now))]
    (cond
      (nil? claims)
      {:status 401 :body {:error "invalid_token"
                          :message "Invalid or expired refresh token"}}

      (<= (:exp claims) now)
      {:status 401 :body {:error "expired_token"
                          :message "Refresh token expired"}}

      :else
      (let [user (user-repo/find-user-by-id db (:user-id claims))]
        (cond
          (nil? user)
          {:status 401 :body {:error "invalid_token"
                              :message "User not found"}}

          (not= (:jti claims) (str (:user/refresh-token user)))
          {:status 401 :body {:error "invalid_token"
                              :message "Refresh token is not valid"}}

          :else
          {:status 200
           :body {:access-token (auth-service/create-access-token (:user-id claims))
                  :refresh-token (auth-service/create-refresh-token (:user-id claims))}})))))

(defn reset-password-handler
  "Reset password user."
  [{{{:keys [email]} :body} :parameters}]

  (if-let [user
           (user-repo/find-user-by-email db email)]

    (let [otp
          (auth-service/generate-otp)

          user-id
          (:user/id user)

          name
          (:user/first-name user)]

      (auth-service/set-otp! user-id otp)
      (email-service/send-otp-email email otp name)

      {:status 200 :body {:message otp}})

    {:status 404 :body {:error "User not found."}}))

(defn otp-handler [{{{:keys [email otp]} :body} :parameters}]
  (if-let [user
           (user-repo/find-user-by-email db email)]

    (let [user-id
          (:user/id user)

          user-otp
          (:user/otp user)

          user-expire-otp
          (:user/otp-expires-at user)]

      (prn user)

      (if (and (not (auth-repo/otp-not-expired? user-expire-otp))
               (= user-otp otp))
        {:status 200 :body {:access-token (auth-service/create-access-token user-id)
                            :refresh-token (auth-service/create-refresh-token user-id)}}
        {:status 400 :body {:message "OTP is invalid or expired."}}))

    {:status 404 :body {:error "User not found."}}))