(ns team-challenge.repository.product-payment-option-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT payment_option_id FROM product_payment_options WHERE product_id=?" product-id]))

(defn add! [product-id payment-option-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_payment_options (product_id, payment_option_id) VALUES (?, ?) RETURNING *" product-id payment-option-id]))

(defn remove! [product-id payment-option-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_payment_options WHERE product_id=? AND payment_option_id=? RETURNING *" product-id payment-option-id]))