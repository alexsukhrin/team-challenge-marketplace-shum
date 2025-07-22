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
            :description "This route does not require authorization."
            :parameters {:body ::auth-domain/register-params}
            :responses {201 {:body ::auth-domain/login-response}
                        409 {:body ::auth-domain/error-response}}
            :handler #'auth-handler/register-handler}}]

   ["/login"
    {:post {:summary "login user"
            :description "This route does not require authorization."
            :parameters {:body ::auth-domain/login-params}
            :responses {200 {:body ::auth-domain/login-response}
                        401 {:body ::auth-domain/error-response}}
            :handler #'auth-handler/login-handler}}]

   ["/confirm"
    {:get {:summary "confirm email with token"
           :description "This route does not require authorization."
           :parameters {:query {:token ::auth-domain/token}}
           :responses {200 {:body ::auth-domain/login-response}
                       400 {:body ::auth-domain/error-response}}
           :handler #'auth-handler/confirm-email-handler}}]

   ["/logout"
    {:post {:summary "logout user"
            :description "This route has require authorization."
            :middleware [middleware/wrap-authentication]
            :responses {200 {:body {:message ::auth-domain/message}}
                        401 {:body {:error ::auth-domain/error}}}
            :handler #'auth-handler/logout-handler}}]

   ["/refresh"
    {:post {:summary "get a new token pair using a refresh token"
            :description "This route does not require authorization."
            :responses {200 {:body ::auth-domain/login-response}
                        401 {:body ::auth-domain/error-response}}
            :parameters {:body ::auth-domain/refresh-params}
            :handler #'auth-handler/refresh-token-handler}}]

   ["/reset"
    {:post {:summary "user create reset link"
            :description "This route does not require authorization."
            :parameters {:body {:email ::auth-domain/email}}
            :response {200 {:body {:message ::auth-domain/message}}
                       400 {:body {:error ::auth-domain/error}}}
            :handler #'auth-handler/reset-password-handler}}]

   ["/otp"
    {:post {:summary "user verify otp"
            :description "This route does not require authorization."
            :parameters {:body {:email ::auth-domain/email
                                :otp ::auth-domain/otp}}
            :response {200 {:body ::auth-domain/login-response}
                       400 {:body {:error ::auth-domain/error}}}
            :handler #'auth-handler/otp-handler}}]])
