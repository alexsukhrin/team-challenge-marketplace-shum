(ns team-challenge.repository.delivery-option-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM delivery_options"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM delivery_options WHERE id=?" id]))

(defn create! [option]
  (jdbc/execute-one! datasource
                     ["INSERT INTO delivery_options (name, description) VALUES (?, ?) RETURNING *"
                      (:name option) (:description option)]))

(defn update! [id option]
  (jdbc/execute-one! datasource
                     ["UPDATE delivery_options SET name=?, description=? WHERE id=? RETURNING *"
                      (:name option) (:description option) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM delivery_options WHERE id=? RETURNING id" id]))