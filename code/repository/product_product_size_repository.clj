;; TODO: Переписати репозиторій під Datomic

(ns team-challenge.repository.product-product-size-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :size_id)
                  (from :product_product_sizes)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id size-id]
  (let [query (-> (insert-into :product_product_sizes)
                  (columns :product_id :size_id)
                  (values [[product-id size-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id size-id]
  (let [query (-> (delete-from :product_product_sizes)
                  (where [:and [:= :product_id product-id]
                               [:= :size_id size-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))