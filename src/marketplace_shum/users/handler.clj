(ns marketplace-shum.users.handler
  (:require
   [marketplace-shum.users.service :as user-service]
   [clojure.core :as c]))

(defn favorite-categories-handler [{:keys [body-params user]}]
  (user-service/update-favorite-categories (:user-id user) body-params)
  {:status 200 :body {:message "Favorite categories updated"}})

(defn get-favorite-categories-handler [{:keys [user]}]
  {:status 200 :body
   {:favorite-categories (user-service/get-favorite-categories (c/parse-uuid (:user-id user)))}})

(defn get-roles-handler [{:keys [user]}]
  {:status 200 :body {:roles (user-service/get-roles (c/parse-uuid (:user-id user)))}})

(defn roles-handler [{:keys [body-params user]}]
  (user-service/update-roles (c/parse-uuid (:user-id user)) body-params)
  {:status 200 :body {:message "Roles updated"}})

(defn update-password-handler [_]
  {:status 200 :body {:message "Password updated"}})