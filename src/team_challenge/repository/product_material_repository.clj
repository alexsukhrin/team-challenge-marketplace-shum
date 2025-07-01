(ns team-challenge.repository.product-material-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_materials"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_materials WHERE id=?" id]))

(defn create! [material]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_materials (name, description) VALUES (?, ?) RETURNING *"
                      (:name material) (:description material)]))

(defn update! [id material]
  (jdbc/execute-one! datasource
                     ["UPDATE product_materials SET name=?, description=? WHERE id=? RETURNING *"
                      (:name material) (:description material) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_materials WHERE id=? RETURNING id" id]))