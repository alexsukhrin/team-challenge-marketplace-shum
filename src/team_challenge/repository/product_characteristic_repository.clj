(ns team-challenge.repository.product-characteristic-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_characteristics"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_characteristics WHERE id=?" id]))

(defn create! [characteristic]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_characteristics (name, description) VALUES (?, ?) RETURNING *"
                      (:name characteristic) (:description characteristic)]))

(defn update! [id characteristic]
  (jdbc/execute-one! datasource
                     ["UPDATE product_characteristics SET name=?, description=? WHERE id=? RETURNING *"
                      (:name characteristic) (:description characteristic) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_characteristics WHERE id=? RETURNING id" id]))