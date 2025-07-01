(ns team-challenge.repository.user-product-category-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-user [user-id]
  (jdbc/execute! datasource ["SELECT category_id FROM user_product_categories WHERE user_id=?" user-id]))

(defn add! [user-id category-id]
  (jdbc/execute-one! datasource ["INSERT INTO user_product_categories (user_id, category_id) VALUES (?, ?) RETURNING *" user-id category-id]))

(defn remove! [user-id category-id]
  (jdbc/execute-one! datasource ["DELETE FROM user_product_categories WHERE user_id=? AND category_id=? RETURNING *" user-id category-id]))