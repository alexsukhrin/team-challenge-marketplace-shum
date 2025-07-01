(ns team-challenge.repository.product-delivery-option-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT delivery_option_id FROM product_delivery_options WHERE product_id=?" product-id]))

(defn add! [product-id delivery-option-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_delivery_options (product_id, delivery_option_id) VALUES (?, ?) RETURNING *" product-id delivery-option-id]))

(defn remove! [product-id delivery-option-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_delivery_options WHERE product_id=? AND delivery_option_id=? RETURNING *" product-id delivery-option-id]))