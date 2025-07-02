(ns team-challenge.repository.user-product-category-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-user [user-id]
  (let [query (-> (select :category_id)
                  (from :user_product_categories)
                  (where [:= :user_id user-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [user-id category-id]
  (let [query (-> (insert-into :user_product_categories)
                  (columns :user_id :category_id)
                  (values [[user-id category-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [user-id category-id]
  (let [query (-> (delete-from :user_product_categories)
                  (where [:and [:= :user_id user-id]
                               [:= :category_id category-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))