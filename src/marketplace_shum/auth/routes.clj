(ns marketplace-shum.auth.routes
  (:require
   [marketplace-shum.auth.handler :as auth-handler]
   [marketplace-shum.auth.domain :as auth-domin]))

(def routes
  ["/auth"
   {:tags ["auth"]}

   ["/register"
    {:post {:summary "register new user"
            :parameters {:body ::auth-domin/register-params}
            :responses {201 {:body ::auth-domin/register-response}
                        409 {:body ::auth-domin/error-response}}
            :handler #'auth-handler/register-handler}}]

   ["/login"
    {:post {:summary "login user"
            :parameters {:body ::auth-domin/login-params}
            :responses {200 {:body ::auth-domin/login-response}
                        401 {:body ::auth-domin/error-response}}
            :handler #'auth-handler/login-handler}}]

   ["/confirm"
    {:get {:summary "confirm email with token"
           :parameters {:query {:token ::auth-domin/token}}
           :responses {200 {:body ::auth-domin/register-response}
                       400 {:body ::auth-domin/error-response}}
           :handler #'auth-handler/confirm-email-handler}}]])