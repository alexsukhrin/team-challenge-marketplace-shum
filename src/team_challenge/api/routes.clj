(ns team-challenge.api.routes
  (:require
   [reitit.ring :as ring]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [team-challenge.api.middleware :as middleware]
   [team-challenge.api.user-controller :as user-controller]
   [reitit.coercion.spec :as spec-coercion]))

(def default-options-handler
  {:options {:handler (fn [_] {:status 200})}})

(def common-middleware
  [middleware/wrap-cors
   middleware/wrap-exceptions
   middleware/wrap-json-response
   middleware/wrap-json-body])

;; --------------------------
;; Swagger routes
;; --------------------------

(def swagger-routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "Marketplace API"
                            :description "swagger api docs"}
                     :securityDefinitions {:apiAuth {:type :apiKey
                                                       :in :header
                                                       :name "authorization"}}
                     :tags [{:name "auth" :description "registration and authorization routes api"}]}
           :handler (swagger/create-swagger-handler)}
     :options default-options-handler}]
   
   ["/api-docs/*"
    {:get {:no-doc true
           :handler (swagger-ui/create-swagger-ui-handler {:url "/swagger.json"})}
     :options default-options-handler}]])

;; --------------------------
;; Auth routes
;; --------------------------

(def auth-routes
  ["/auth"
   {:tags #{"auth"}}

   {:get {:handler (fn [_] {:status 200 :body "ok"})}
    :options default-options-handler}

   ["/register"
    {:post {:summary "Register a new user"
            :description "This route does not require authorization."
            :parameters {:body ::user-controller/register-params}
            :response {200 {:body {:message string?}}}
            :handler #'user-controller/register-user-handler}
     :options default-options-handler}]

   ["/login"
    {:post {:summary "Login and get token pair"
            :parameters {:body ::user-controller/login-params}
            :handler #'user-controller/login-handler}
     :options default-options-handler}]

   ["/confirm-email"
    {:post {:summary "Confirm email with token"
            :parameters {:body ::user-controller/confirm-email-params}
            :handler #'user-controller/confirm-email-handler}
     :options default-options-handler}]

   ["/logout"
    {:post {:summary "Logout user"
            :handler #'user-controller/logout-handler}
     :options default-options-handler}]

   ["/refresh"
    {:post {:summary "Get a new token pair using a refresh token"
            :parameters {:body ::user-controller/refresh-params}
            :handler #'user-controller/refresh-token-handler}
     :options default-options-handler}]

   ["/request-password-reset"
    {:post {:summary "Request password reset"
            :parameters {:body ::user-controller/request-reset-params}
            :handler #'user-controller/request-password-reset-handler}
     :options default-options-handler}]

   ["/reset-password"
    {:post {:summary "Reset password with token"
            :parameters {:body ::user-controller/reset-password-params}
            :handler #'user-controller/reset-password-handler}
     :options default-options-handler}]])

;; --------------------------
;; User routes
;; --------------------------

(def user-routes
  ["/users"
   {:get {:handler (fn [_] {:status 200 :body "ok"})}
    :options default-options-handler}

   ["/me"
    {:get {:summary "Get current user info (protected)"
           :middleware [middleware/wrap-authentication]
           :handler #'user-controller/get-current-user-handler}
     :options default-options-handler}]])

;; --------------------------
;; API router
;; --------------------------

(def api-routes
  ["/api/v1"
   {:get {:handler (fn [_] {:status 200 :body "ok"})}
    :options default-options-handler}
   auth-routes
   user-routes])

;; --------------------------
;; Main router
;; --------------------------

(defn make-routes []
  (ring/router
   [["" {:middleware common-middleware
         :get {:handler (fn [_] {:status 302
                                 :headers {"Location" "/api-docs/"}
                                 :body ""})}
         :options {:strip-extra-keys false
                   :handler (fn [_] {:status 200})}}
     ;; Add sections here
     swagger-routes
     api-routes
     ]]
   {:data {:coercion spec-coercion/coercion}
    :exception
    {:coercion
     {:default
      (fn [e _]
        {:status 400
         :body {:error "validation"
                :message "Invalid request data"
                :details (ex-data e)}})}}}))
