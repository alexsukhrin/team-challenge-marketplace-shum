(ns marketplace-shum.users.handler
  (:require
   [marketplace-shum.users.service :as user-service]))

(defn favorite-categories-handler [{:keys [body-params user]}]
  (user-service/update-favorite-categories (:user-id user) body-params)
  {:status 200 :body {:message "Favorite categories updated"}})
