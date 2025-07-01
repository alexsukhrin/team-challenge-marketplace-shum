(ns team-challenge.repository.product-color-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_colors"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_colors WHERE id=?" id]))

(defn create! [color]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_colors (name, hex, description) VALUES (?, ?, ?) RETURNING *"
                      (:name color) (:hex color) (:description color)]))

(defn update! [id color]
  (jdbc/execute-one! datasource
                     ["UPDATE product_colors SET name=?, hex=?, description=? WHERE id=? RETURNING *"
                      (:name color) (:hex color) (:description color) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_colors WHERE id=? RETURNING id" id]))