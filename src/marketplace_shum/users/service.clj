(ns marketplace-shum.users.service
  (:require
   [marketplace-shum.users.repository :as user-repo]
   [marketplace-shum.infra.db :refer [db]]))

(defn update-favorite-categories [user-id favorite-categories]
  (->> (:favorite-categories favorite-categories)
       (mapv keyword)
       (user-repo/update-favorite-categories! db user-id)))
