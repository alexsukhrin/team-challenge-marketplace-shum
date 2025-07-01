(ns team-challenge.repository.product-product-size-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT size_id FROM product_product_sizes WHERE product_id=?" product-id]))

(defn add! [product-id size-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_product_sizes (product_id, size_id) VALUES (?, ?) RETURNING *" product-id size-id]))

(defn remove! [product-id size-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_product_sizes WHERE product_id=? AND size_id=? RETURNING *" product-id size-id]))