(ns marketplace-shum.auth.routes
  (:require
   [marketplace-shum.auth.handler :as auth-handler]
   [marketplace-shum.auth.domain :as auth-domain]
   [marketplace-shum.web.middlewares :as middleware]))

(def routes
  ["/auth"
   {:tags ["auth"]}

   ["/register"
    {:post {:summary "register new user"
            :parameters {:body ::auth-domain/register-params}
            :responses {201 {:body ::auth-domain/register-response}
                        409 {:body ::auth-domain/error-response}}
            :handler #'auth-handler/register-handler}}]

   ["/login"
    {:post {:summary "login user"
            :parameters {:body ::auth-domain/login-params}
            :responses {200 {:body ::auth-domain/login-response}
                        401 {:body ::auth-domain/error-response}}
            :handler #'auth-handler/login-handler}}]

   ["/confirm"
    {:get {:summary "confirm email with token"
           :parameters {:query {:token ::auth-domain/token}}
           :responses {200 {:body ::auth-domain/register-response}
                       400 {:body ::auth-domain/error-response}}
           :handler #'auth-handler/confirm-email-handler}}]

   ["/logout"
    {:post {:summary "Logout user"
            :middleware [middleware/wrap-authentication]
            :responses {200 {:body {:message string?}}
                        401 {:body {:error string?}}}
            :handler #'auth-handler/logout-handler}}]

   ["/refresh"
    {:post {:summary "Get a new token pair using a refresh token"
            :parameters {:body ::auth-domain/refresh-params}
            :handler #'auth-handler/refresh-token-handler}}]])