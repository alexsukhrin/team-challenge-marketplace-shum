(ns team-challenge.repository.product-product-clothing-size-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :clothing_size_id)
                  (from :product_product_clothing_sizes)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id clothing-size-id]
  (let [query (-> (insert-into :product_product_clothing_sizes)
                  (columns :product_id :clothing_size_id)
                  (values [[product-id clothing-size-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id clothing-size-id]
  (let [query (-> (delete-from :product_product_clothing_sizes)
                  (where [:and [:= :product_id product-id]
                               [:= :clothing_size_id clothing-size-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))