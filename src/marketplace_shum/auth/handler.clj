(ns marketplace-shum.auth.handler
  (:require
   [marketplace-shum.auth.service :as auth-service]
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.infra.db :refer [db]]
   [marketplace-shum.email.service :as email-service]))

(defn register-handler [{{{:keys [first-name last-name password email]} :body} :parameters}]
  (let [user (user-repo/find-user-by-email db email)]
    (cond
      (nil? user)
      (let [{:keys [email-confirmation-token]}
            (user-repo/create-user! db {:first-name first-name
                                        :last-name last-name
                                        :password (auth-service/hash-password password)
                                        :email email})]
        (email-service/send-confirmation-email email email-confirmation-token first-name)
        {:status 201
         :body {:message "User registered. Please check your email for a confirmation link."}})

      (not (:user/email-confirmed? user))
      (let [{:keys [email-confirmation-token]} (user-repo/update-confirmation-token! db (:db/id user))]

        (email-service/send-confirmation-email email email-confirmation-token (:user/first-name user))
        {:status 200
         :body {:message "Confirmation email re-sent."}})

      :else
      {:status 409
       :body {:error "conflict"
              :message "A user with this email already exists."}})))

(defn login-handler [request]
  (let [login-data (:body request)]
    {:status 200
     :body {:message "Login successful"
            :token "jwt-token-here"}}))

(defn confirm-email-handler [{{:keys [token]} :query-params}]
  (if-let [user (user-repo/find-user-by-email-confirmation-token db token)]
    (let [expiry (:user/email-confirmation-expiry user)
          now (java.util.Date.)]
      (cond
        (:user/email-confirmed? user)
        {:status 200 :body {:message "Email already confirmed"}}

        (and expiry (.before expiry now))
        {:status 410 :body {:message "Confirmation token expired"}}

        :else
        (do
          (user-repo/set-email-confirmed! db (:user/id user) true)
          {:status 200 :body {:message "Email confirmed successfully"}})))
    {:status 404 :body {:message "Token not found"}}))
