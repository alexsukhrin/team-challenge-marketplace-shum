(ns team-challenge.repository.product-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all
  ([] (let [query (-> (select :*)
                      (from :products)
                      sql/format)]
        (jdbc/execute! datasource query)))
  ([filters]
   (let [where-clauses (reduce (fn [acc [k v]]
                                 (conj acc [:= (keyword k) v]))
                               []
                               filters)
         query (cond-> (select :*)
                 (seq where-clauses) (where (into [:and] where-clauses))
                 true (from :products))
         sql-query (sql/format query)]
     (jdbc/execute! datasource sql-query))))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :products)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [product]
  (let [query (-> (insert-into :products)
                  (columns :user_id :name :description :price :quantity :category_id :brand_id :condition_id)
                  (values [[(:user_id product) (:name product) (:description product) (:price product) (:quantity product) (:category_id product) (:brand_id product) (:condition_id product)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn update! [id product]
  (let [query (-> (update :products)
                  (set {:user_id (:user_id product)
                        :name (:name product)
                        :description (:description product)
                        :price (:price product)
                        :quantity (:quantity product)
                        :category_id (:category_id product)
                        :brand_id (:brand_id product)
                        :condition_id (:condition_id product)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [id]
  (let [query (-> (delete-from :products)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))