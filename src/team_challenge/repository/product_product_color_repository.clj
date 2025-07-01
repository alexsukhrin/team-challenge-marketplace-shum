(ns team-challenge.repository.product-product-color-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT color_id FROM product_product_colors WHERE product_id=?" product-id]))

(defn add! [product-id color-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_product_colors (product_id, color_id) VALUES (?, ?) RETURNING *" product-id color-id]))

(defn remove! [product-id color-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_product_colors WHERE product_id=? AND color_id=? RETURNING *" product-id color-id]))