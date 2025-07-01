(ns team-challenge.repository.product-categories-rel-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT category_id FROM product_categories_rel WHERE product_id=?" product-id]))

(defn add! [product-id category-id]
  (jdbc/execute-one! datasource ["INSERT INTO product_categories_rel (product_id, category_id) VALUES (?, ?) RETURNING *" product-id category-id]))

(defn remove! [product-id category-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_categories_rel WHERE product_id=? AND category_id=? RETURNING *" product-id category-id]))