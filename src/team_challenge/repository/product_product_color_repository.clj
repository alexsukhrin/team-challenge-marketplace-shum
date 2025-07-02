(ns team-challenge.repository.product-product-color-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :color_id)
                  (from :product_product_colors)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id color-id]
  (let [query (-> (insert-into :product_product_colors)
                  (columns :product_id :color_id)
                  (values [[product-id color-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id color-id]
  (let [query (-> (delete-from :product_product_colors)
                  (where [:and [:= :product_id product-id]
                               [:= :color_id color-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))