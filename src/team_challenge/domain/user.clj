(ns team-challenge.domain.user
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]))

(defrecord User [first-name last-name email password])

(defn full-name
  "Fullname user's."
  [^User user]
  (str (:first-name user) " " (:last-name user)))

(defn new-user
  "New user."
  [{:keys [password] :as user-data}]
  (map->User (update user-data :password (fn [_] (hashers/derive password)))))