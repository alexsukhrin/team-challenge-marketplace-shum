(ns marketplace-shum.users.handler
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::user uuid?)
(s/def ::password-hash (s/and string? #(> (count %) 20)))
(s/def ::verification-token (s/nilable string?))
(s/def ::password-reset-token (s/nilable string?))
(s/def ::password-reset-token-expires-at (s/nilable inst?))
(s/def ::auth
  (s/keys :req [::user ::password-hash]
          :opt [::verification-token
                ::password-reset-token
                ::password-reset-token-expires-at]))
(s/def ::email string?)
(s/def ::password string?)
(s/def ::first_name string?)
(s/def ::last_name string?)
(s/def ::message string?)
(s/def ::register-params (s/keys :req-un [::first_name ::last_name ::email ::password]))

;; (defn create-auth!
;;   [conn {:keys [user password-hash verification-token password-reset-token password-reset-token-expires-at]}]
;;   (d/transact conn {:tx-data [(merge
;;                                {:auth/user user
;;                                 :auth/password-hash password-hash}
;;                                (when verification-token {:auth/verification-token verification-token})
;;                                (when password-reset-token {:auth/password-reset-token password-reset-token})
;;                                (when password-reset-token-expires-at {:auth/password-reset-token-expires-at password-reset-token-expires-at}))]}))

;; (defn find-auth-by-user
;;   [db user-id]
;;   (d/pull db '[*] [:auth/user user-id]))

;; (defn set-verification-token!
;;   [conn user-id token]
;;   (d/transact conn {:tx-data [[:db/add [:auth/user user-id] :auth/verification-token token]]}))

;; (defn set-password-reset-token!
;;   [conn user-id token expires-at]
;;   (d/transact conn {:tx-data [[:db/add [:auth/user user-id] :auth/password-reset-token token]
;;                               [:db/add [:auth/user user-id] :auth/password-reset-token-expires-at expires-at]]}))

;; (defn create-auth!
;;   [conn {:keys [user password-hash verification-token]}]
;;   (auth-repo/create-auth! conn {:user user
;;                                 :password-hash password-hash
;;                                 :verification-token verification-token}))

;; (defn get-auth-by-user
;;   [db user-id]
;;   (auth-repo/find-auth-by-user db user-id))

;; (defn set-verification-token!
;;   [conn user-id token]
;;   (auth-repo/set-verification-token! conn user-id token))

;; (defn set-password-reset-token!
;;   [conn user-id token expires-at]
;;   (auth-repo/set-password-reset-token! conn user-id token expires-at))

(defn register-handler [request]
  (let [user-data (:body request)]
    (try
      ;;(user-service/register-user user-data)
      {:status 201
       :body {:message "User registered. Please check your email for a confirmation link."}}
      (catch clojure.lang.ExceptionInfo e
        (if (= (:type (ex-data e)) :email-conflict)
          {:status 409
           :body {:error "conflict"
                  :message "A user with this email already exists."}}
          (throw e))))))

(def routes
  ["/auth"
   ["/register"
    {:post {:summary "register new user"
            :parameters {:email ::email}
            :responses {201 {:body {:message ::message}}}
            :handler #'register-handler}}]])
