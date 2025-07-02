(ns team-challenge.repository.product-characteristic-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (let [query (-> (select :*)
                  (from :product_characteristics)
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :product_characteristics)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [characteristic]
  (let [query (-> (insert-into :product_characteristics)
                  (columns :name :description)
                  (values [[(:name characteristic) (:description characteristic)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn update! [id characteristic]
  (let [query (-> (update :product_characteristics)
                  (set {:name (:name characteristic)
                        :description (:description characteristic)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [id]
  (let [query (-> (delete-from :product_characteristics)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))