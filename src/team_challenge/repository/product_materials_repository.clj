(ns team-challenge.repository.product-materials-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :characteristic_id :value)
                  (from :product_materials)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id characteristic-id value]
  (let [query (-> (insert-into :product_materials)
                  (columns :product_id :characteristic_id :value)
                  (values [[product-id characteristic-id value]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id characteristic-id]
  (let [query (-> (delete-from :product_materials)
                  (where [:and [:= :product_id product-id]
                               [:= :characteristic_id characteristic-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))