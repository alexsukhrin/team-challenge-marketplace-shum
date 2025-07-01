(ns team-challenge.repository.product-clothing-size-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_clothing_sizes"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_clothing_sizes WHERE id=?" id]))

(defn create! [size]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_clothing_sizes (name, description) VALUES (?, ?) RETURNING *"
                      (:name size) (:description size)]))

(defn update! [id size]
  (jdbc/execute-one! datasource
                     ["UPDATE product_clothing_sizes SET name=?, description=? WHERE id=? RETURNING *"
                      (:name size) (:description size) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_clothing_sizes WHERE id=? RETURNING id" id]))