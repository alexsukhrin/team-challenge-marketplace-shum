(ns team-challenge.repository.product-photo-repository
  (:require [next.jdbc :as jdbc]
            [team-challenge.db :refer [datasource]]))

(defn get-all-for-product [product-id]
  (jdbc/execute! datasource ["SELECT * FROM product_photos WHERE product_id=?" product-id]))

(defn create! [photo]
  (jdbc/execute-one! datasource
                     ["INSERT INTO product_photos (product_id, url, s3_key) VALUES (?, ?, ?) RETURNING *"
                      (:product_id photo) (:url photo) (:s3_key photo)]))

(defn delete! [photo-id]
  (jdbc/execute-one! datasource ["DELETE FROM product_photos WHERE id=? RETURNING *" photo-id]))