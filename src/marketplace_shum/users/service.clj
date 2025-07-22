(ns marketplace-shum.users.service
  (:require
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.auth.service :as auth-service]
   [marketplace-shum.infra.db :refer [db]]))

(defn update-favorite-categories [user-id favorite-categories]
  (->> (:favorite-categories favorite-categories)
       (mapv keyword)
       (user-repo/update-attributes! db user-id :user/favorite-categories)))

(defn update-roles [user-id roles]
  (->> (:roles roles)
       (mapv keyword)
       (user-repo/update-attributes! db user-id :user/roles)))

(defn get-favorite-categories [user-id]
  (user-repo/get-attributes db user-id :user/favorite-categories))

(defn get-roles [user-id]
  (user-repo/get-attributes db user-id :user/roles))

(defn update-password [user-id password]
  (user-repo/set-password! db user-id (auth-service/hash-password password)))