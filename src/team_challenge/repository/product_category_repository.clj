(ns team-challenge.repository.product-category-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (let [query (-> (select :*)
                  (from :product_categories)
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :product_categories)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [category]
  (let [query (-> (insert-into :product_categories)
                  (columns :name :photo)
                  (values [[(:name category) (:photo category)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn update! [id category]
  (let [query (-> (update :product_categories)
                  (set {:name (:name category)
                        :photo (:photo category)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [id]
  (let [query (-> (delete-from :product_categories)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))