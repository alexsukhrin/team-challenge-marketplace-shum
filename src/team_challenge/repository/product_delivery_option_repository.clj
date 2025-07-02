(ns team-challenge.repository.product-delivery-option-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :delivery_option_id)
                  (from :product_delivery_options)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id delivery-option-id]
  (let [query (-> (insert-into :product_delivery_options)
                  (columns :product_id :delivery_option_id)
                  (values [[product-id delivery-option-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id delivery-option-id]
  (let [query (-> (delete-from :product_delivery_options)
                  (where [:and [:= :product_id product-id]
                               [:= :delivery_option_id delivery-option-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))