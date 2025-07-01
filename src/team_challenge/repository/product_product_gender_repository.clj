(ns team-challenge.repository.product-product-gender-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT gender_id FROM product_product_genders WHERE product_id=?" product-id]))

(defn add! [product-id gender-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_product_genders (product_id, gender_id) VALUES (?, ?) RETURNING *" product-id gender-id]))

(defn remove! [product-id gender-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_product_genders WHERE product_id=? AND gender_id=? RETURNING *" product-id gender-id]))