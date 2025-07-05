(ns team-challenge.repository.product-payment-option-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :payment_option_id)
                  (from :product_payment_options)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id payment-option-id]
  (let [query (-> (insert-into :product_payment_options)
                  (columns :product_id :payment_option_id)
                  (values [[product-id payment-option-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id payment-option-id]
  (let [query (-> (delete-from :product_payment_options)
                  (where [:and [:= :product_id product-id]
                               [:= :payment_option_id payment-option-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))