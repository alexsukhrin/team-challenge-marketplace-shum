(ns team-challenge.db
  (:require [mount.core :refer [defstate]]
            [team-challenge.config :as config]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defstate datasource
  :start (let [db-spec (get-in config/*config* [:db :spec])]
           (println "Postgres DB spec:" db-spec)
           (-> (jdbc/get-datasource db-spec)
               (jdbc/with-options {:return-keys true 
                                   ;:builder-fn rs/as-unqualified-lower-maps
                                   })))
  :stop nil)
