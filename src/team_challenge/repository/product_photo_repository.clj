(ns team-challenge.repository.product-photo-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (let [query (-> (select :*)
                  (from :product_photos)
                  (where [:= :product_id product-id])
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn create! [photo]
  (let [query (-> (insert-into :product_photos)
                  (columns :product_id :url :s3_key)
                  (values [[(:product_id photo) (:url photo) (:s3_key photo)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [photo-id]
  (let [query (-> (delete-from :product_photos)
                  (where [:= :id photo-id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))