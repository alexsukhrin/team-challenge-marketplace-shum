(ns team-challenge.repository.product-product-clothing-size-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT clothing_size_id FROM product_product_clothing_sizes WHERE product_id=?" product-id]))

(defn add! [product-id clothing-size-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_product_clothing_sizes (product_id, clothing_size_id) VALUES (?, ?) RETURNING *" product-id clothing-size-id]))

(defn remove! [product-id clothing-size-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_product_clothing_sizes WHERE product_id=? AND clothing_size_id=? RETURNING *" product-id clothing-size-id]))