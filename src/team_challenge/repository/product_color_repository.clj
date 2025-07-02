(ns team-challenge.repository.product-color-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (let [query (-> (select :*)
                  (from :product_colors)
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :product_colors)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [color]
  (let [query (-> (insert-into :product_colors)
                  (columns :name :hex :description)
                  (values [[(:name color) (:hex color) (:description color)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn update! [id color]
  (let [query (-> (update :product_colors)
                  (set {:name (:name color)
                        :hex (:hex color)
                        :description (:description color)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [id]
  (let [query (-> (delete-from :product_colors)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))