(ns team-challenge.repository.product-gender-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (jdbc/execute! datasource ["SELECT * FROM product_genders"]))

(defn get-by-id [id]
  (jdbc/execute-one! datasource ["SELECT * FROM product_genders WHERE id=?" id]))

(defn create! [gender]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_genders (name, description) VALUES (?, ?) RETURNING *"
                      (:name gender) (:description gender)]))

(defn update! [id gender]
  (jdbc/execute-one! datasource
                     ["UPDATE product_genders SET name=?, description=? WHERE id=? RETURNING *"
                      (:name gender) (:description gender) id]))

(defn delete! [id]
  (jdbc/execute-one! datasource ["DELETE FROM product_genders WHERE id=? RETURNING id" id]))