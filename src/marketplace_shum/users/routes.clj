(ns marketplace-shum.users.routes
  (:require
   [marketplace-shum.users.handler :as user-handler]
   [marketplace-shum.web.middlewares :as middleware]))

(def routes
  ["/users"
   {:tags ["users"]}

   ;;  ["/update-password"
   ;;   {:patch {:summary "user update password"
   ;;            :description "This route requires authorization."
   ;;            :parameters {:body {:password ::user/password}}
   ;;            :response {200 {:body {:message string?}}
   ;;                       400 {:body {:error string?}}}
   ;;            :handler (fn [{:keys [user parameters]}]
   ;;                       (let [{{:keys [password]} :body} parameters]
   ;;                         (handler/update-password (:email user) password)))}
   ;;    :swagger {:security [{:apiAuth []}]}}]

   ["/favorite-categories"
    {:post {:summary "user favorite categories"
            :description "This route requires authorization."
            :middleware [middleware/wrap-authentication]
            :parameters {:body {:favorite-categories [string?]}}
            :response {200 {:body {:message string?}}
                       400 {:body {:error string?}}}
            :handler #'user-handler/favorite-categories-handler}
     :swagger {:security [{:apiAuth []}]}}]])