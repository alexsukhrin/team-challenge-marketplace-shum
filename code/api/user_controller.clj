(ns team-challenge.api.user-controller
  (:require [team-challenge.service.user-service :as user-service]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [team-challenge.repository.user-product-category-repository :as user-product-category-repo]))

(defn spec-errors->messages [explain-data]
  (let [problems (:clojure.spec.alpha/problems explain-data)
        missing-keys (->> problems
                          (mapcat :pred)
                          (filter #(and (coll? %) (some #{'clojure.core/contains?} %)))
                          (mapcat rest)
                          (filter keyword?)
                          (map name)
                          set)]
    (vec
     (concat
      ;; For missing keys
      (for [k missing-keys]
        {:field k
         :error "field is required"})
      ;; For other problems
      (for [{:keys [path pred val]} problems
            :let [field (if-let [f (first path)] (name f) "request")]
            :when (not (and (coll? pred) (some #{'clojure.core/contains?} pred)))]
        {:field field
         :error (cond
                  (= pred 'string?) "must be a string"
                  (= pred 'clojure.core/not-empty) "cannot be empty"
                  (and (= pred 'clojure.core/not) (nil? val)) "field is required"
                  (instance? clojure.lang.LazySeq pred) "invalid or missing required fields"
                  :else (str "failed predicate: " pred))})))))

(s/def ::first_name string?)
(s/def ::last_name string?)
(s/def ::email string?)
(s/def ::password string?)
(s/def ::refresh-token string?)
(s/def ::register-params (s/keys :req-un [::first_name ::last_name ::email ::password]))
(s/def ::login-params (s/keys :req-un [::email ::password]))
(s/def ::refresh-params (s/keys :req-un [::refresh-token]))
(s/def ::request-reset-params (s/keys :req-un [::email]))
(s/def ::reset-password-params (s/keys :req-un [::token ::password]))
(s/def ::confirm-email-params (s/keys :req-un [::token]))
(s/def ::token string?)

(defn register-user-handler
  "Handles user registration. Returns message on success."
  [request]
  (let [user-data (:body request)]
    (if (s/valid? ::register-params user-data)
      (try
        (user-service/register-user user-data)
        {:status 201
         :body {:message "User registered. Please check your email for a confirmation link."}}
        (catch clojure.lang.ExceptionInfo e
          (if (= (:type (ex-data e)) :email-conflict)
            {:status 409
             :body {:error "conflict"
                    :message "A user with this email already exists."}}
            ;; Re-throw other exceptions
            (throw e))))
      (let [problems (s/explain-data ::register-params user-data)]
        {:status 400
         :body {:error "validation"
                :message "Invalid input: email, first_name, last_name, and password are required."
                :fields (spec-errors->messages problems)}}))))

(defn login-handler
  "Handles user login and returns a JWT."
  [request]
  (let [{:keys [email password]} (:body request)]
    (if-let [tokens (user-service/login email password)]
      {:status 200 :body tokens}
      {:status 401 :body {:error "unauthorized"
                          :message "Invalid credentials or email not confirmed"}})))

(defn logout-handler
  "Handles user logout."
  [request]
  (let [auth-header (get-in request [:headers "authorization"])
        [scheme token] (when auth-header (str/split auth-header #" " 2))]
    (cond
      (nil? auth-header)
      {:status 400 :body {:error "token_missing"
                          :message "Authorization header is required"}}

      (not= (str/lower-case scheme) "bearer")
      {:status 400 :body {:error "invalid_scheme"
                          :message "Authorization scheme must be Bearer"}}

      (or (nil? token) (str/blank? token))
      {:status 400 :body {:error "token_missing"
                          :message "Bearer token is missing"}}

      :else
      (do (user-service/logout token)
          {:status 200 :body {:message "Logged out successfully"}}))))

(defn refresh-token-handler
  "Handles token refreshment."
  [request]
  (let [refresh-token (get-in request [:body-params :refresh-token])]
    (if-let [tokens (user-service/refresh-tokens refresh-token)]
      {:status 200 :body tokens}
      {:status 401 :body {:error "invalid_token"
                          :message "Invalid or expired refresh token"}})))

(defn request-password-reset-handler
  "Handles password reset requests."
  [request]
  (let [email (get-in request [:body-params :email])]
    (if-let [token (user-service/request-password-reset email)]
      {:status 200 :body {:message "Password reset token sent (for dev, here is the token)"
                          :token token}}
      {:status 404 :body {:error "not_found"
                          :message "User with this email not found"}})))

(defn reset-password-handler
  "Handles password reset submissions."
  [request]
  (let [{:keys [token password]} (:body-params request)]
    (if (user-service/reset-password token password)
      {:status 200 :body {:message "Password has been reset successfully"}}
      {:status 400 :body {:error "invalid_token"
                          :message "Invalid or expired token"}})))

(defn confirm-email-handler
  "Handles email confirmation."
  [request]
  (let [token (get-in request [:query-params :token])]
    (if (user-service/confirm-email token)
      {:status 200 :body {:message "Email confirmed successfully. You can now log in."}}
      {:status 400 :body {:error "invalid_token"
                          :message "Invalid or expired confirmation token."}})))

(defn get-current-user-handler
  "Retrieves the current user's information from the JWT."
  [request]
  (let [identity (:identity request)]
    {:status 200 :body {:user identity}}))

(defn list-user-product-categories [{{:keys [user_id]} :path-params}]
  {:status 200
   :body (user-product-category-repo/get-all-for-user user_id)})

(defn add-user-product-category [{{:keys [user_id]} :path-params :keys [body-params]}]
  (let [category-id (:category_id body-params)]
    (try
      (let [result (user-product-category-repo/add! user_id category-id)]
        {:status 201 :body result})
      (catch Exception e
        {:status 400 :body {:error (.getMessage e)}}))))

(defn remove-user-product-category [{{:keys [user_id category_id]} :path-params}]
  (try
    (if-let [result (user-product-category-repo/remove! user_id category_id)]
      {:status 204}
      {:status 404 :body {:error "User category not found"}})
    (catch Exception e
      {:status 400 :body {:error (.getMessage e)}})))

(comment
  (def user-data {:first_name "Alexandr",
                  :last_name "Sukhryn",
                  :email "alexandrvirtual@gmail.com",
                  :password "password1986"})
  (s/valid? ::register-params user-data)

  (register-user-handler {:body user-data}))