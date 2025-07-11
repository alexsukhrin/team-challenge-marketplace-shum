;; TODO: Переписати репозиторій під Datomic

(ns team-challenge.repository.product-material-repository
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h :refer [select from where insert-into columns values update set delete-from]]
            [team-challenge.db :refer [datasource]]))

(defn get-all []
  (let [query (-> (select :*)
                  (from :materials)
                  sql/format)]
    (jdbc/execute! datasource query)))

(defn get-by-id [id]
  (let [query (-> (select :*)
                  (from :materials)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query)))

(defn create! [material]
  (let [query (-> (insert-into :materials)
                  (columns :name :description)
                  (values [[(:name material) (:description material)]])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn update! [id material]
  (let [query (-> (update :materials)
                  (set {:name (:name material)
                        :description (:description material)})
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))

(defn delete! [id]
  (let [query (-> (delete-from :materials)
                  (where [:= :id id])
                  sql/format)]
    (jdbc/execute-one! datasource query {:return-keys true})))