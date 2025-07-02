(ns team-challenge.repository.product-categories-rel-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :category_id)
                  (from :product_categories_rel)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id category-id]
  (let [query (-> (insert-into :product_categories_rel)
                  (columns :product_id :category_id)
                  (values [[product-id category-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id category-id]
  (let [query (-> (delete-from :product_categories_rel)
                  (where [:and [:= :product_id product-id]
                               [:= :category_id category-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))