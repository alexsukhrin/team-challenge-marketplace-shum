(ns team-challenge.repository.product-product-material-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT material_id FROM product_product_materials WHERE product_id=?" product-id]))

(defn add! [product-id material-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_product_materials (product_id, material_id) VALUES (?, ?) RETURNING *" product-id material-id]))

(defn remove! [product-id material-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_product_materials WHERE product_id=? AND material_id=? RETURNING *" product-id material-id]))