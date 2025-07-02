(ns team-challenge.repository.product-product-material-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :material_id)
                  (from :product_product_materials)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id material-id]
  (let [query (-> (insert-into :product_product_materials)
                  (columns :product_id :material_id)
                  (values [[product-id material-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id material-id]
  (let [query (-> (delete-from :product_product_materials)
                  (where [:and [:= :product_id product-id]
                               [:= :material_id material-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))