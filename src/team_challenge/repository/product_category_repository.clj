(ns team-challenge.repository.product-category-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_categories"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_categories WHERE id=?" id]))

(defn create! [category]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_categories (name, photo) VALUES (?, ?) RETURNING *"
                      (:name category) (:photo category)]))

(defn update! [id category]
  (jdbc/execute-one! datasource
                     ["UPDATE product_categories SET name=?, photo=? WHERE id=? RETURNING *"
                      (:name category) (:photo category) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_categories WHERE id=? RETURNING id" id]))