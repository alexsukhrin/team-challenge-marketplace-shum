(ns marketplace-shum.users.routes
  (:require
   [marketplace-shum.users.handler :as user-handler]
   [marketplace-shum.web.middlewares :as middleware]))

(def routes
  ["/users"
   {:tags ["users"]}

   ["/update-password"
    {:patch {:summary "user update password"
             :description "This route requires authorization."
             :parameters {:body {:password string?}}
             :response {200 {:body {:message string?}}
                        400 {:body {:error string?}}}
             :handler #'user-handler/update-password-handler}
     :swagger {:security [{:apiAuth []}]}}]

   ["/favorite-categories"
    {:post {:summary "user favorite categories"
            :description "This route requires authorization."
            :middleware [middleware/wrap-authentication]
            :parameters {:body {:favorite-categories [string?]}}
            :response {200 {:body {:message string?}}
                       400 {:body {:error string?}}}
            :handler #'user-handler/favorite-categories-handler}

     :swagger {:security [{:apiAuth []}]}

     :get {:summary "Get user's favorite categories"
           :middleware [middleware/wrap-authentication]
           :response {200 {:body {:favorite-categories [string?]}}}
           :handler #'user-handler/get-favorite-categories-handler}}]

   ["/roles"
    {:post {:summary "add user roles"
            :description "This route requires authorization."
            :middleware [middleware/wrap-authentication]
            :parameters {:body {:roles [string?]}}
            :response {200 {:body {:message string?}}
                       400 {:body {:error string?}}}
            :handler #'user-handler/roles-handler}

     :get {:summary "Get user's roles"
           :middleware [middleware/wrap-authentication]
           :response {200 {:body {:roles [string?]}}}
           :handler #'user-handler/get-roles-handler}

     :swagger {:security [{:apiAuth []}]}}]])