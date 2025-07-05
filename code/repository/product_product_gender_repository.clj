;; TODO: Переписати репозиторій під Datomic

(ns team-challenge.repository.product-product-gender-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :gender_id)
                  (from :product_product_genders)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn add! [product-id gender-id]
  (let [query (-> (insert-into :product_product_genders)
                  (columns :product_id :gender_id)
                  (values [[product-id gender-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn remove! [product-id gender-id]
  (let [query (-> (delete-from :product_product_genders)
                  (where [:and [:= :product_id product-id]
                               [:= :gender_id gender-id]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))