(ns team-challenge.repository.product-repository
  (:require [next.jdbc :as jdbc]
            [clojure.string :as str]
            [team-challenge.db :refer [datasource]]))

(defn- build-where-clause [filters]
  (let [clauses (->> filters
                     (map (fn [[k v]]
                            (str (name k) "=?")))
                     (str/join " AND "))
        values (mapv val filters)]
    (if (seq filters)
      [(str " WHERE " clauses) values]
      ["" []])))

(defn get-all
  ([] (jdbc/execute! datasource ["SELECT * FROM products"]))
  ([filters]
   (let [[where-clause values] (build-where-clause filters)
         sql (str "SELECT * FROM products" where-clause)]
     (jdbc/execute! datasource (into [sql] values)))))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM products WHERE id=?" id]))

(defn create! [product]
  (jdbc/execute-one! datasource
                     ["INSERT INTO products (user_id, name, description, price, quantity, category_id, brand_id, condition_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING *"
                      (:user_id product) (:name product) (:description product) (:price product) (:quantity product) (:category_id product) (:brand_id product) (:condition_id product)]))

(defn update! [id product]
  (jdbc/execute-one! datasource
                     ["UPDATE products SET user_id=?, name=?, description=?, price=?, quantity=?, category_id=?, brand_id=?, condition_id=? WHERE id=? RETURNING *"
                      (:user_id product) (:name product) (:description product) (:price product) (:quantity product) (:category_id product) (:brand_id product) (:condition_id product) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM products WHERE id=? RETURNING id" id]))