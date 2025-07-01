(ns team-challenge.repository.product-materials-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT characteristic_id, value FROM product_materials WHERE product_id=?" product-id]))

(defn add! [product-id characteristic-id value]
  (jdbc/execute-one! datasource ["INSERT INTO product_materials (product_id, characteristic_id, value) VALUES (?, ?, ?) RETURNING *" product-id characteristic-id value]))

(defn remove! [product-id characteristic-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_materials WHERE product_id=? AND characteristic_id=? RETURNING *" product-id characteristic-id]))