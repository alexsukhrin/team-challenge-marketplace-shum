(ns marketplace-shum.auth.handler
  (:require
   [clojure.spec.alpha :as s]
   [marketplace-shum.auth.service :as auth-service]
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.infra.db :refer [db]]
   [marketplace-shum.email.service :as email-service]))

(s/def ::email string?)
(s/def ::password string?)
(s/def ::first-name string?)
(s/def ::last-name string?)
(s/def ::message string?)
(s/def ::error string?)
(s/def ::token string?)
(s/def ::register-params (s/keys :req-un [::first-name ::last-name ::email ::password]))
(s/def ::register-response (s/keys :req-un [::message]))
(s/def ::error-response (s/keys :req-un [::error ::message]))
(s/def ::login-params (s/keys :req-un [::email ::password]))
(s/def ::login-response (s/keys :req-un [::message ::token]))

(defn register-handler [{{{:keys [first-name last-name password email]} :body} :parameters}]
  (when (user-repo/user-exists? db email)
    {:status 409
     :body {:error "conflict"
            :message "A user with this email already exists."}})

  (let [{:keys [email-confirmation-token]}
        (user-repo/create-user! db {:first-name first-name
                                    :last-name last-name
                                    :password (auth-service/hash-password password)
                                    :email email})]
    (email-service/send-confirmation-email email email-confirmation-token first-name)
    {:status 201
     :body {:message "User registered. Please check your email for a confirmation link."}}))

(defn login-handler [request]
  (let [login-data (:body request)]
    {:status 200
     :body {:message "Login successful"
            :token "jwt-token-here"}}))

(defn confirm-email-handler [request]
  (let [token (get-in request [:path-params :token])]
    {:status 200
     :body {:message "Email confirmed successfully"}}))

(def routes
  ["/auth"
   {:tags ["auth"]}

   ["/register"
    {:post {:summary "register new user"
            :parameters {:body ::register-params}
            :responses {201 {:body ::register-response}
                        409 {:body ::error-response}}
            :handler #'register-handler}}]

   ["/login"
    {:post {:summary "login user"
            :parameters {:body ::login-params}
            :responses {200 {:body ::login-response}
                        401 {:body ::error-response}}
            :handler #'login-handler}}]

   ["/confirm/:token"
    {:get {:summary "confirm email with token"
           :parameters {:path {:token string?}}
           :responses {200 {:body ::register-response}
                       400 {:body ::error-response}}
           :handler #'confirm-email-handler}}]])
